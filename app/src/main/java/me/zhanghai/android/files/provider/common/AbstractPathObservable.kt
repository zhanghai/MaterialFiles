/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import android.os.Handler
import android.os.HandlerThread
import me.zhanghai.android.files.util.ThrottledRunnable
import java.io.IOException

abstract class AbstractPathObservable(private val intervalMillis: Long) : PathObservable {
    private val observers = mutableMapOf<() -> Unit, ThrottledRunnable>()

    private var isClosed = false

    private val lock = Any()

    override fun addObserver(observer: () -> Unit) {
        synchronized(lock) {
            ensureOpenLocked()
            observers[observer] = ThrottledRunnable(handler, intervalMillis, observer)
        }
    }

    override fun removeObserver(observer: () -> Unit) {
        synchronized(lock) {
            ensureOpenLocked()
            observers.remove(observer)?.cancel()
        }
    }

    protected fun notifyObservers() {
        synchronized(lock) {
            for (observer in observers.values) {
                observer()
            }
        }
    }

    private fun ensureOpenLocked() {
        if (isClosed) {
            throw ClosedDirectoryObserverException()
        }
    }

    @Throws(IOException::class)
    override fun close() {
        synchronized(lock) {
            if (isClosed) {
                return
            }
            observers.values.forEach { it.cancel() }
            observers.clear()
            onCloseLocked()
            isClosed = true
        }
    }

    @Throws(IOException::class)
    protected abstract fun onCloseLocked()

    companion object {
        private var notifier = Notifier()

        internal val handler: Handler
            get() = notifier.handler

        init {
            notifier.start()
        }
    }

    private class Notifier : HandlerThread("AbstractPathObservable.Notifier") {
        val handler by lazy { Handler(looper) }

        init {
            isDaemon = true
        }
    }
}
