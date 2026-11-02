/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.rclone.daemon

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import me.zhanghai.android.files.filejob.FileJobService
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.storage.RcloneStorage
import me.zhanghai.android.files.util.valueCompat
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object RcloneDaemon {
    private const val TAG = "RcloneDaemon"
    private const val DEFAULT_EXTERNAL_URL = "http://127.0.0.1:5572/"

    private val lock = Any()
    private var process: Process? = null
    private var appContext: Context? = null

    @Volatile
    var baseUrl: String? = null
        private set

    fun initialize(context: Context) {
        appContext = context.applicationContext
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        if (hasRcloneStorages()) {
                            try {
                                ensureRunning()
                            } catch (e: Exception) {
                                Log.w(TAG, "Failed to start rclone daemon on app start", e)
                            }
                        }
                    }
                    Lifecycle.Event.ON_STOP -> {
                        if (FileJobService.runningJobCount == 0) {
                            stop()
                        }
                    }
                    else -> {}
                }
            }
        )
        Runtime.getRuntime().addShutdownHook(Thread { stop() })
    }

    fun hasRcloneStorages(): Boolean =
        Settings.STORAGES.valueCompat.any { it is RcloneStorage }

    fun getConfigFile(context: Context): File =
        File(context.filesDir, "rclone/rclone.conf")

    fun saveConfigFile(context: Context, content: String) {
        val file = getConfigFile(context)
        file.parentFile?.mkdirs()
        file.writeText(content)
    }

    fun findBinary(context: Context): File? {
        val nativeLib = File(context.applicationInfo.nativeLibraryDir, "librclone.so")
        if (nativeLib.exists() && nativeLib.canExecute()) {
            return nativeLib
        }
        val appBinary = File(context.filesDir, "rclone/rclone")
        if (appBinary.exists() && appBinary.canExecute()) {
            return appBinary
        }
        val candidatePaths = listOf(
            "/data/data/com.termux/files/usr/bin/rclone",
            "/system/bin/rclone",
            "/system/xbin/rclone",
            "/usr/bin/rclone",
            "/usr/local/bin/rclone"
        )
        for (path in candidatePaths) {
            val file = File(path)
            if (file.exists() && file.canExecute()) {
                return file
            }
        }
        try {
            val proc = Runtime.getRuntime().exec(arrayOf("which", "rclone"))
            val line = proc.inputStream.bufferedReader().readLine()?.trim()
            if (!line.isNullOrEmpty()) {
                val file = File(line)
                if (file.exists() && file.canExecute()) {
                    return file
                }
            }
        } catch (e: Exception) {
            // Ignored.
        }
        return null
    }

    fun isExternalDaemonRunning(): Boolean {
        return try {
            val url = URL("${DEFAULT_EXTERNAL_URL}core/version")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 500
            conn.readTimeout = 500
            conn.requestMethod = "POST"
            conn.responseCode == 200
        } catch (e: Exception) {
            false
        }
    }

    fun isAvailable(context: Context): Boolean =
        findBinary(context) != null || isExternalDaemonRunning()

    @Throws(IOException::class)
    fun ensureRunning(context: Context? = appContext) {
        val ctx = context ?: appContext ?: throw IOException("App context not available")
        synchronized(lock) {
            if (process?.isAlive == true && baseUrl != null) {
                return
            }
            if (isExternalDaemonRunning()) {
                baseUrl = DEFAULT_EXTERNAL_URL
                return
            }
            val binary = findBinary(ctx)
                ?: throw IOException("Rclone binary not found. Place rclone in app directory or install Termux rclone.")
            val configFile = getConfigFile(ctx)
            if (!configFile.exists()) {
                throw FileNotFoundException("rclone.conf not found at ${configFile.absolutePath}")
            }
            val cacheDir = File(ctx.cacheDir, "rclone").apply { mkdirs() }
            val command = listOf(
                binary.absolutePath,
                "rcd",
                "--rc-serve",
                "--rc-no-auth",
                "--rc-addr", "127.0.0.1:0",
                "--cache-dir", cacheDir.absolutePath,
                "--config", configFile.absolutePath
            )
            val processBuilder = ProcessBuilder(command)
            processBuilder.environment()["TMPDIR"] = ctx.cacheDir.absolutePath
            processBuilder.redirectErrorStream(true)
            val proc = processBuilder.start()
            process = proc

            val readyLatch = CountDownLatch(1)
            var startedUrl: String? = null
            val reader = BufferedReader(InputStreamReader(proc.inputStream))

            val readerThread = Thread {
                try {
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        val currentLine = line ?: break
                        Log.d(TAG, "rclone: $currentLine")
                        if (startedUrl == null && currentLine.contains("Serving remote control on")) {
                            val regex = Regex("""Serving remote control on (https?://[^\s/]+)""")
                            val match = regex.find(currentLine)
                            if (match != null) {
                                startedUrl = match.groupValues[1] + "/"
                                readyLatch.countDown()
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignored on process termination.
                } finally {
                    readyLatch.countDown()
                }
            }
            readerThread.isDaemon = true
            readerThread.start()

            val ready = readyLatch.await(10, TimeUnit.SECONDS)
            if (!ready || startedUrl == null || !proc.isAlive) {
                stop()
                throw IOException("Timed out or failed waiting for rclone rcd daemon to start")
            }
            baseUrl = startedUrl
            Log.i(TAG, "Rclone daemon started successfully at $baseUrl")
        }
    }

    fun restart(context: Context? = appContext) {
        stop()
        ensureRunning(context)
    }

    fun stop() {
        synchronized(lock) {
            process?.let {
                try {
                    it.destroy()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        it.destroyForcibly()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error stopping rclone daemon", e)
                }
                process = null
            }
            baseUrl = null
            Log.i(TAG, "Rclone daemon stopped")
        }
    }
}
