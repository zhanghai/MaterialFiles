/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.channels.SeekableByteChannel
import java8.nio.file.Path
import java8.nio.file.StandardWatchEventKinds
import java8.nio.file.WatchEvent
import me.zhanghai.android.files.provider.FileSystemProviders
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer

class LocalWatchService : AbstractWatchService<LocalWatchKey>() {
    private val keys = mutableMapOf<Path, LocalWatchKey>()

    init {
        synchronized(services) { services.add(this) }
    }

    @Throws(IOException::class)
    fun register(
        path: Path,
        kinds: Array<WatchEvent.Kind<*>>,
        vararg modifiers: WatchEvent.Modifier
    ): LocalWatchKey {
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
        synchronized(keys) {
            var key = keys[path]
            if (key != null) {
                key.kinds = kindSet
            } else {
                key = LocalWatchKey(this, path, kindSet)
                keys[path] = key
            }
            return key
        }
    }

    override fun cancel(key: LocalWatchKey) {
        synchronized(keys) { keys.remove(key.watchable())!! }
    }

    @Throws(IOException::class)
    override fun onClose() {
        synchronized(keys) { keys.clear() }
        synchronized(services) { services.remove(this) }
    }

    private fun onWatchEvent(path: Path, kind: WatchEvent.Kind<Path>) {
        val pathParent = path.parent
        synchronized(keys) {
            for ((keyPath, key) in keys) {
                if (keyPath == path || keyPath == pathParent) {
                    if (kind !in key.kinds) {
                        continue
                    }
                    if (FileSystemProviders.overflowWatchEvents) {
                        key.addEvent(StandardWatchEventKinds.OVERFLOW, null)
                    } else {
                        key.addEvent(kind, path)
                    }
                }
            }
        }
    }

    companion object {
        private val services = mutableSetOf<LocalWatchService>()

        fun onEntryCreated(path: Path) {
            onWatchEvent(path, StandardWatchEventKinds.ENTRY_CREATE)
        }

        fun onEntryDeleted(path: Path) {
            onWatchEvent(path, StandardWatchEventKinds.ENTRY_DELETE)
        }

        fun onEntryModified(path: Path) {
            onWatchEvent(path, StandardWatchEventKinds.ENTRY_MODIFY)
        }

        private fun onWatchEvent(path: Path, kind: WatchEvent.Kind<Path>) {
            synchronized(services) {
                services.forEach { it.onWatchEvent(path, kind) }
            }
        }
    }
}

fun NotifyEntryModifiedSeekableByteChannel(
    channel: SeekableByteChannel,
    path: Path
) : SeekableByteChannel =
    if (channel is ForceableChannel) {
        NotifyEntryModifiedForceableSeekableByteChannel(channel, path)
    } else {
        NotifyEntryModifiedNonForceableSeekableByteChannel(channel, path)
    }

private class NotifyEntryModifiedNonForceableSeekableByteChannel(
    channel: SeekableByteChannel,
    private val path: Path
) : DelegateNonForceableSeekableByteChannel(channel) {
    override fun write(src: ByteBuffer): Int {
        return super.write(src).also {
            LocalWatchService.onEntryModified(path)
        }
    }

    override fun truncate(size: Long): SeekableByteChannel {
        return super.truncate(size).also {
            LocalWatchService.onEntryModified(path)
        }
    }

    override fun close() {
        super.close()

        LocalWatchService.onEntryModified(path)
    }
}

private class NotifyEntryModifiedForceableSeekableByteChannel(
    channel: SeekableByteChannel,
    private val path: Path
) : DelegateForceableSeekableByteChannel(channel) {
    override fun write(src: ByteBuffer): Int {
        return super.write(src).also {
            LocalWatchService.onEntryModified(path)
        }
    }

    override fun truncate(size: Long): SeekableByteChannel {
        return super.truncate(size).also {
            LocalWatchService.onEntryModified(path)
        }
    }

    override fun close() {
        super.close()

        LocalWatchService.onEntryModified(path)
    }
}

class NotifyEntryModifiedOutputStream(
    outputStream: OutputStream,
    private val path: Path
) : DelegateOutputStream(outputStream) {
    override fun write(b: Int) {
        super.write(b)

        LocalWatchService.onEntryModified(path)
    }

    override fun write(b: ByteArray) {
        super.write(b)

        LocalWatchService.onEntryModified(path)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        super.write(b, off, len)

        LocalWatchService.onEntryModified(path)
    }

    override fun flush() {
        super.flush()

        LocalWatchService.onEntryModified(path)
    }

    override fun close() {
        super.close()

        LocalWatchService.onEntryModified(path)
    }
}
