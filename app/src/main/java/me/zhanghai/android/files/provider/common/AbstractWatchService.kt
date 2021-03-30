/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.ClosedWatchServiceException
import java8.nio.file.WatchEvent
import java8.nio.file.WatchKey
import java8.nio.file.WatchService
import java8.nio.file.Watchable
import java.io.IOException
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

abstract class AbstractWatchService<K : AbstractWatchKey<K, *>> : WatchService {
    private val queue = LinkedBlockingQueue<WatchKey>()

    @Volatile
    private var isClosed = false

    private val lock = Any()

    fun enqueue(key: K) {
        queue.offer(key)
    }

    abstract fun cancel(key: K)

    override fun poll(): WatchKey? {
        ensureOpen()
        return checkClosedKey(queue.poll())
    }

    @Throws(InterruptedException::class)
    override fun poll(timeout: Long, unit: TimeUnit): WatchKey? {
        ensureOpen()
        return checkClosedKey(queue.poll(timeout, unit))
    }

    @Throws(InterruptedException::class)
    override fun take(): WatchKey {
        ensureOpen()
        return checkClosedKey(queue.take())
    }

    private fun <T : WatchKey?> checkClosedKey(key: T): T {
        if (key == KEY_CLOSED) {
            // There may be other threads still waiting for a key.
            queue.offer(key)
        }
        ensureOpen()
        return key
    }

    private fun ensureOpen() {
        if (isClosed) {
            throw ClosedWatchServiceException()
        }
    }

    @Throws(IOException::class)
    override fun close() {
        synchronized(lock) {
            if (isClosed) {
                return
            }
            onClose()
            isClosed = true
            queue.clear()
            queue.offer(KEY_CLOSED)
        }
    }

    @Throws(IOException::class)
    protected abstract fun onClose()

    companion object {
        private val KEY_CLOSED: WatchKey = DummyKey()
    }

    private class DummyKey : WatchKey {
        override fun isValid(): Boolean {
            throw AssertionError()
        }

        override fun pollEvents(): List<WatchEvent<*>> {
            throw AssertionError()
        }

        override fun reset(): Boolean {
            throw AssertionError()
        }

        override fun cancel() {
            throw AssertionError()
        }

        override fun watchable(): Watchable {
            throw AssertionError()
        }
    }
}
