/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.DirectoryIteratorException
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.StandardWatchEventKinds
import java8.nio.file.WatchEvent
import java8.nio.file.attribute.BasicFileAttributes
import me.zhanghai.android.files.BuildConfig
import me.zhanghai.android.files.provider.FileSystemProviders
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.atomic.AtomicInteger

class PollingWatchService : AbstractWatchService<PollingWatchKey>() {
    private val pollers = mutableMapOf<Path, Poller>()

    @Throws(IOException::class)
    fun register(
        path: Path,
        kinds: Array<WatchEvent.Kind<*>>,
        vararg modifiers: WatchEvent.Modifier
    ): PollingWatchKey {
        val kindSet = mutableSetOf<WatchEvent.Kind<*>>()
        for (kind in kinds) {
            when (kind) {
                StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY -> kindSet += kind
                // Ignored.
                StandardWatchEventKinds.OVERFLOW -> {}
                else -> throw UnsupportedOperationException(kind.name())
            }
        }
        for (modifier in modifiers) {
            throw UnsupportedOperationException(modifier.name())
        }
        synchronized(pollers) {
            var poller = pollers[path]
            if (poller != null) {
                poller.kinds = kindSet
            } else {
                poller = Poller(this, path, kindSet)
                pollers[path] = poller
                poller.start()
            }
            return poller.key
        }
    }

    private fun removePoller(poller: Poller) {
        // TODO: kotlinc: Type mismatch.
        //synchronized(pollers) { pollers -= poller.key.watchable() }
        synchronized(pollers) { pollers.remove(poller.key.watchable()) }
    }

    override fun cancel(key: PollingWatchKey) {
        val poller = synchronized(pollers) { pollers.remove(key.watchable())!! }
        poller.interrupt()
        try {
            poller.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    override fun onClose() {
        // Don't keep synchronized on pollers, or we may get a deadlock when joining.
        val pollers = synchronized(pollers) {
            pollers.values.toList().also { pollers.clear() }
        }
        var exception: IOException? = null
        for (poller in pollers) {
            poller.interrupt()
            try {
                poller.join()
            } catch (e: InterruptedException) {
                val newException = InterruptedIOException().apply { initCause(e) }
                if (exception == null) {
                    exception = newException
                } else {
                    exception.addSuppressed(newException)
                }
            }
        }
        exception?.let { throw it }
    }

    private class Poller @Throws(IOException::class) constructor(
        private val watchService: PollingWatchService,
        private val path: Path,
        @Volatile
        var kinds: Set<WatchEvent.Kind<*>>
    ) : Thread("AbstractPollingWatchService.Poller-${id.getAndIncrement()}") {
        val key = PollingWatchKey(watchService, path)

        private var oldFiles: Map<Path, BasicFileAttributes>

        init {
            isDaemon = true
            oldFiles = getFiles()
        }

        override fun run() {
            try {
                while (true) {
                    sleep(POLL_INTERNAL_MILLIS)
                    val newFiles = getFiles()
                    if (FileSystemProviders.overflowWatchEvents) {
                        if (newFiles != oldFiles) {
                            key.addEvent(StandardWatchEventKinds.OVERFLOW, null)
                        }
                    } else {
                        for ((path, oldAttributes) in oldFiles) {
                            val newAttributes = newFiles[path]
                            val kind = when {
                                newAttributes == null -> StandardWatchEventKinds.ENTRY_DELETE
                                newAttributes != oldAttributes ->
                                    StandardWatchEventKinds.ENTRY_MODIFY
                                else -> continue
                            }
                            if (kind !in kinds) {
                                continue
                            }
                            key.addEvent(kind, path)
                        }
                        for (path in newFiles.keys) {
                            if (path in oldFiles) {
                                continue
                            }
                            val kind = StandardWatchEventKinds.ENTRY_CREATE
                            if (kind !in kinds) {
                                continue
                            }
                            key.addEvent(kind, path)
                        }
                    }
                    oldFiles = newFiles
                }
            } catch (e: Exception) {
                e.printStackTrace()
                key.setInvalid()
                if (!(e is InterruptedException || e is InterruptedIOException)) {
                    key.signal()
                }
                watchService.removePoller(this)
            }
        }

        @Throws(IOException::class)
        private fun getFiles(): Map<Path, BasicFileAttributes> =
            mutableMapOf<Path, BasicFileAttributes>().apply {
                if (path.isDirectory(LinkOption.NOFOLLOW_LINKS)) {
                    path.newDirectoryStream().use { directoryStream ->
                        try {
                            directoryStream.forEach {
                                val attributes = try {
                                    it.readAttributes(
                                        BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS
                                    )
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                    return@forEach
                                }
                                this[it] = attributes
                            }
                        } catch (e: DirectoryIteratorException) {
                            throw e.cause!!
                        }
                    }
                } else {
                    this[path] = path.readAttributes(
                        BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS
                    )
                }
            }.also {
                if (BuildConfig.DEBUG) {
                    // Ensure that the attributes class has overridden equals().
                    val attributes = it.values.firstOrNull() ?: return@also
                    check(
                        attributes::class.java.getMethod("equals", Object::class.java)
                            != Object::class.java.getMethod("equals", Object::class.java)
                    )
                }
            }

        companion object {
            private const val POLL_INTERNAL_MILLIS = 1000L

            private val id = AtomicInteger()
        }
    }
}
