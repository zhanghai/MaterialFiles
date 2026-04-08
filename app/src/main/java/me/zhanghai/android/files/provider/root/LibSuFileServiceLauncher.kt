/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.topjohnwu.superuser.NoShellException
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import me.zhanghai.android.files.provider.remote.IRemoteFileService
import me.zhanghai.android.files.provider.remote.RemoteFileServiceInterface
import me.zhanghai.android.files.provider.remote.RemoteFileSystemException
import me.zhanghai.android.files.util.createIntent
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object LibSuFileServiceLauncher {
    private val lock = Any()
    private const val TAG = "LibSuLauncher"

    init {
        Shell.enableVerboseLogging = true
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setInitializers(LibSuShellInitializer::class.java)
                .setFlags(Shell.FLAG_MOUNT_MASTER or Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(TimeUnit.MILLISECONDS.toSeconds(RootFileService.TIMEOUT_MILLIS))
        )
    }

    fun isSuAvailable(): Boolean =
        // @see com.topjohnwu.superuser.Shell.rootAccess
        try {
            Runtime.getRuntime().exec("su --version")
            Log.d(TAG, "su binary found on device")
            true
        } catch (e: IOException) {
            // java.io.IOException: Cannot run program "su": error=2, No such file or directory
            Log.d(TAG, "No su binary found on device")
            false
        }

    @Throws(RemoteFileSystemException::class)
    fun launchService(): IRemoteFileService {
        Log.d(TAG, "Attempting to launch root service")

        synchronized(lock) {
            // libsu won't call back when su isn't available.
            if (!isSuAvailable()) {
                Log.w(TAG, "Root isn't available - throwing exception")
                throw RemoteFileSystemException("Root isn't available")
            }
            Log.d(TAG, "Root is available, proceeding with service launch")

            return try {
                runBlocking {
                    try {
                        withTimeout(RootFileService.TIMEOUT_MILLIS) {
                            // Proactively create the shell because RootService doesn't allow us to
                            // handle errors during shell creation.
                            suspendCancellableCoroutine<Unit> { continuation ->
                                // Shell.getShell(GetShellCallback) doesn't allow handling errors.
                                Shell.EXECUTOR.submit {
                                    try {
                                        Shell.getShell()
                                        continuation.resume(Unit)
                                    } catch (e: NoShellException) {
                                        Log.w(TAG, "NoShellException: ${e.message}")
                                        continuation.resumeWithException(
                                            RemoteFileSystemException(e)
                                        )
                                    }
                                }
                            }
                            suspendCancellableCoroutine { continuation ->
                                val intent = LibSuFileService::class.createIntent()
                                val connection = object : ServiceConnection {
                                    override fun onServiceConnected(
                                        name: ComponentName,
                                        service: IBinder
                                    ) {
                                        Log.d(TAG, "Root service connected successfully")
                                        val serviceInterface =
                                            IRemoteFileService.Stub.asInterface(service)
                                        continuation.resume(serviceInterface)
                                    }

                                    override fun onServiceDisconnected(name: ComponentName) {
                                        Log.w(TAG, "Root service disconnected")
                                        if (continuation.isActive) {
                                            continuation.resumeWithException(
                                                RemoteFileSystemException(
                                                    "libsu service disconnected"
                                                )
                                            )
                                        }
                                    }

                                    override fun onBindingDied(name: ComponentName) {
                                        Log.w(TAG, "Root service binding died")
                                        if (continuation.isActive) {
                                            continuation.resumeWithException(
                                                RemoteFileSystemException("libsu binding died")
                                            )
                                        }
                                    }

                                    override fun onNullBinding(name: ComponentName) {
                                        Log.w(TAG, "Root service binding is null")
                                        if (continuation.isActive) {
                                            continuation.resumeWithException(
                                                RemoteFileSystemException("libsu binding is null")
                                            )
                                        }
                                    }
                                }
                                launch(Dispatchers.Main.immediate) {
                                    Log.d(TAG, "Binding to root service")
                                    RootService.bind(intent, connection)
                                    continuation.invokeOnCancellation {
                                        Log.d(TAG, "Service binding cancelled, unbinding")
                                        launch(Dispatchers.Main.immediate) {
                                            RootService.unbind(connection)
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: TimeoutCancellationException) {
                        Log.w(TAG, "Timeout while launching root service: ${e.message}")
                        throw RemoteFileSystemException(e)
                    }
                }
            } catch (e: InterruptedException) {
                Log.w(TAG, "Interrupted while launching root service: ${e.message}")
                throw RemoteFileSystemException(e)
            }
        }
    }
}

private class LibSuShellInitializer : Shell.Initializer() {
    // Prevent normal shells from being created and set as the main shell.
    override fun onInit(context: Context, shell: Shell): Boolean = shell.isRoot
}

class LibSuFileService : RootService() {
    override fun onCreate() {
        super.onCreate()
        Log.d("LibSuFileService", "Root file service created")
        RootFileService.main()
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d("LibSuFileService", "Root file service bound")
        return RemoteFileServiceInterface()
    }
}