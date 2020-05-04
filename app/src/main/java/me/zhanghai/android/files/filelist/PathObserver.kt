/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import java8.nio.file.Path
import me.zhanghai.android.files.provider.common.PathObservable
import me.zhanghai.android.files.provider.common.observe
import me.zhanghai.android.files.util.closeSafe
import java.io.Closeable
import java.io.IOException

class PathObserver(path: Path, @MainThread onChange: () -> Unit) : Closeable {
    private var pathObservable: PathObservable? = null

    private var closed = false
    private val lock = Any()

    init {
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            synchronized(lock) {
                if (closed) {
                    return@execute
                }
                pathObservable = try {
                    path.observe(THROTTLE_INTERVAL_MILLIS)
                } catch (e: UnsupportedOperationException) {
                    // Ignored.
                    return@execute
                } catch (e: IOException) {
                    // Ignored.
                    e.printStackTrace()
                    return@execute
                }.apply {
                    val mainHandler = Handler(Looper.getMainLooper())
                    addObserver { mainHandler.post(onChange) }
                }
            }
        }
    }

    override fun close() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            synchronized(lock) {
                if (closed) {
                    return@execute
                }
                closed = true
                pathObservable?.closeSafe()
            }
        }
    }

    companion object {
        private const val THROTTLE_INTERVAL_MILLIS = 1000L
    }
}
