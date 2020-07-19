/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.Path
import java8.nio.file.StandardWatchEventKinds
import java8.nio.file.WatchEvent
import java8.nio.file.WatchKey

abstract class AbstractWatchKey(
    protected open val watchService: AbstractWatchService,
    private val path: Path
) : WatchKey {
    private var isSignaled = false

    private var events = mutableListOf<Event<*>>()

    protected val lock = Any()

    fun <T> addEvent(kind: WatchEvent.Kind<T>, context: T?) {
        synchronized(lock) {
            if (events.isNotEmpty()) {
                val lastEvent = events.last()
                if (lastEvent.kind() == StandardWatchEventKinds.OVERFLOW
                    || (lastEvent.kind() == kind && lastEvent.context() == context)) {
                    lastEvent.repeat()
                    return
                }
            }
            if (kind === StandardWatchEventKinds.OVERFLOW || events.size >= MAX_PENDING_EVENTS) {
                events.clear()
                events.add(Event(StandardWatchEventKinds.OVERFLOW, null))
                signal()
                return
            }
            events.add(Event(kind, context))
            signal()
        }
    }

    fun signal() {
        synchronized(lock) {
            if (!isSignaled) {
                isSignaled = true
                watchService.enqueue(this)
            }
        }
    }

    override fun pollEvents(): List<WatchEvent<*>> {
        synchronized(lock) {
            val events = events
            this.events = mutableListOf()
            return events
        }
    }

    override fun reset(): Boolean {
        synchronized(lock) {
            val isValid = isValid
            if (isValid && isSignaled) {
                if (events.isEmpty()) {
                    isSignaled = false
                } else {
                    watchService.enqueue(this)
                }
            }
            return isValid
        }
    }

    override fun watchable(): Path = path

    private class Event<T> constructor(
        private val kind: WatchEvent.Kind<T>,
        private val context: T?
    ) : WatchEvent<T> {
        private var count = 1

        private val lock = Any()

        override fun kind(): WatchEvent.Kind<T> = kind

        override fun context(): T? = context

        override fun count(): Int = count

        fun repeat() {
            synchronized(lock) { ++count }
        }
    }

    companion object {
        private const val MAX_PENDING_EVENTS = 256
    }
}
