/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import me.zhanghai.android.files.BuildConfig
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.provider.FileSystemProviders
import me.zhanghai.android.files.provider.linux.syscall.Syscalls
import me.zhanghai.android.files.provider.remote.IRemoteFileService
import me.zhanghai.android.files.provider.remote.RemoteFileServiceInterface
import me.zhanghai.android.files.provider.remote.RemoteFileSystemException
import me.zhanghai.android.libselinux.SeLinux
import rikka.shizuku.Shizuku
import rikka.sui.Sui
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object SuiFileServiceLauncher {
    private const val TIMEOUT_MILLIS = 10 * 1000

    private val suiInitializationLock = Any()
    private var isSuiInitialized = false

    private val launchServiceLock = Any()

    val isSuiAvailable: Boolean
        get() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return false
            }
            ensureSuiInitialized()
            return Sui.isSui()
        }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun ensureSuiInitialized() {
        synchronized(suiInitializationLock) {
            if (!isSuiInitialized) {
                Sui.init(application.packageName)
                isSuiInitialized = true
            }
        }
    }

    @Throws(RemoteFileSystemException::class)
    fun launchService(): IRemoteFileService {
        synchronized(launchServiceLock) {
            if (!isSuiAvailable) {
                throw RemoteFileSystemException("Sui isn't available")
            }
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                val granted = runBlocking<Boolean> {
                    suspendCancellableCoroutine { continuation ->
                        val listener = object : Shizuku.OnRequestPermissionResultListener {
                            override fun onRequestPermissionResult(
                                requestCode: Int,
                                grantResult: Int
                            ) {
                                Shizuku.removeRequestPermissionResultListener(this)
                                val granted = grantResult == PackageManager.PERMISSION_GRANTED
                                continuation.resume(granted)
                            }
                        }
                        Shizuku.addRequestPermissionResultListener(listener)
                        continuation.invokeOnCancellation {
                            Shizuku.removeRequestPermissionResultListener(listener)
                        }
                        Shizuku.requestPermission(listener.hashCode())
                    }
                }
                if (!granted) {
                    throw RemoteFileSystemException("Sui permission isn't granted")
                }
            }
            return runBlocking {
                try {
                    withTimeout(TIMEOUT_MILLIS.toLong()) {
                        suspendCancellableCoroutine { continuation ->
                            val serviceArgs = Shizuku.UserServiceArgs(
                                ComponentName(application, SuiFileServiceInterface::class.java)
                            )
                                .processNameSuffix("sui")
                                .version(BuildConfig.VERSION_CODE)
                                .debuggable(BuildConfig.DEBUG)
                            val connection = object : ServiceConnection {
                                override fun onServiceConnected(
                                    name: ComponentName,
                                    service: IBinder
                                ) {
                                    val serviceInterface = IRemoteFileService.Stub.asInterface(
                                        service
                                    )
                                    continuation.resume(serviceInterface)
                                }

                                override fun onServiceDisconnected(name: ComponentName) {
                                    if (continuation.isActive) {
                                        continuation.resumeWithException(
                                            RemoteFileSystemException("Sui service disconnected")
                                        )
                                    }
                                }

                                override fun onBindingDied(name: ComponentName) {
                                    if (continuation.isActive) {
                                        continuation.resumeWithException(
                                            RemoteFileSystemException("Sui binding died")
                                        )
                                    }
                                }

                                override fun onNullBinding(name: ComponentName) {
                                    if (continuation.isActive) {
                                        continuation.resumeWithException(
                                            RemoteFileSystemException("Sui binding is null")
                                        )
                                    }
                                }
                            }
                            Shizuku.bindUserService(serviceArgs, connection)
                            continuation.invokeOnCancellation {
                                Shizuku.unbindUserService(serviceArgs, connection, true)
                            }
                        }
                    }
                } catch (e: TimeoutCancellationException) {
                    throw RemoteFileSystemException(e)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
class SuiFileServiceInterface : RemoteFileServiceInterface() {
    init {
        Log.i(LOG_TAG, "Loading native libraries")
        System.loadLibrary(Syscalls.libraryName)
        System.loadLibrary(SeLinux.getLibraryName())
        Log.i(LOG_TAG, "Installing file system providers")
        FileSystemProviders.install()
        FileSystemProviders.overflowWatchEvents = true
    }

    companion object {
        private val LOG_TAG = SuiFileServiceInterface::class.java.simpleName
    }
}
