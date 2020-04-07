/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.ClosedWatchServiceException
import java8.nio.file.Path
import java8.nio.file.StandardWatchEventKinds
import java8.nio.file.WatchService
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class WatchServicePathObservable(path: Path, intervalMillis: Long) : AbstractPathObservable(
    intervalMillis
) {
    private val watchService: WatchService
    private val poller: Poller

    init {
        var watchService: WatchService? = null
        var poller: Poller? = null
        var successful = false
        try {
            watchService = path.fileSystem.newWatchService()
            this.watchService = watchService
            path.register(
                watchService, StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY
            )
            poller = Poller()
            this.poller = poller
            poller.start()
            successful = true
        } finally {
            if (!successful) {
                poller?.interrupt()
                watchService?.close()
            }
        }
    }

    @Throws(IOException::class)
    override fun onCloseLocked() {
        poller.interrupt()
        watchService.close()
    }

    companion object {
        private val pollerId = AtomicInteger()
    }

    private inner class Poller : Thread(
        "WatchServicePathObservable.Poller-${pollerId.getAndIncrement()}"
    ) {
        init {
            isDaemon = true
        }

        override fun run() {
            while (true) {
                val key = try {
                    watchService.take()
                } catch (e: ClosedWatchServiceException) {
                    break
                } catch (e: InterruptedException) {
                    break
                }
                if (key.pollEvents().isNotEmpty()) {
                    notifyObservers()
                }
                if (!key.reset()) {
                    break
                }
            }
        }
    }
}
