/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux

import android.system.OsConstants
import android.system.StructPollfd
import java8.nio.file.ClosedWatchServiceException
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.StandardWatchEventKinds
import java8.nio.file.WatchEvent
import java8.nio.file.attribute.BasicFileAttributes
import kotlinx.coroutines.runBlocking
import me.zhanghai.android.files.provider.FileSystemProviders
import me.zhanghai.android.files.provider.common.AbstractWatchService
import me.zhanghai.android.files.provider.common.readAttributes
import me.zhanghai.android.files.provider.linux.syscall.Constants
import me.zhanghai.android.files.provider.linux.syscall.SyscallException
import me.zhanghai.android.files.provider.linux.syscall.Syscalls
import me.zhanghai.android.files.util.hasBits
import java.io.Closeable
import java.io.FileDescriptor
import java.io.IOException
import java.io.InterruptedIOException
import java.util.LinkedList
import java.util.Queue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class LocalLinuxWatchService : AbstractWatchService<LocalLinuxWatchKey>() {
    private val poller = Poller(this)

    init {
        poller.start()
    }

    @Throws(IOException::class)
    fun register(
        path: LinuxPath,
        kinds: Array<WatchEvent.Kind<*>>,
        vararg modifiers: WatchEvent.Modifier
    ): LocalLinuxWatchKey {
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
        return poller.register(path, kindSet)
    }

    override fun cancel(key: LocalLinuxWatchKey) {
        poller.cancel(key)
    }

    @Throws(IOException::class)
    override fun onClose() {
        poller.close()
    }

    private class Poller(
        private val watchService: LocalLinuxWatchService
    ) : Thread("LocalLinuxWatchService.Poller-${id.getAndIncrement()}"), Closeable {
        private val socketFds: Array<FileDescriptor>

        private var inotifyFd: FileDescriptor

        private val keys = mutableMapOf<Int, LocalLinuxWatchKey>()

        private val inotifyBuffer = ByteArray(4 * 1024)

        private val runnables: Queue<() -> Unit> = LinkedList()

        private var isClosed = false

        private val lock = Any()

        init {
            isDaemon = true
            try {
                socketFds = Syscalls.socketpair(OsConstants.AF_UNIX, OsConstants.SOCK_STREAM, 0)
                val flags = Syscalls.fcntl(socketFds[0], OsConstants.F_GETFL)
                if (!flags.hasBits(OsConstants.O_NONBLOCK)) {
                    Syscalls.fcntl(
                        socketFds[0], OsConstants.F_SETFL, flags or OsConstants.O_NONBLOCK
                    )
                }
                inotifyFd = Syscalls.inotify_init1(OsConstants.O_NONBLOCK)
            } catch (e: SyscallException) {
                throw e.toFileSystemException(null)
            }
        }

        @Throws(IOException::class)
        fun register(path: LinuxPath, kinds: Set<WatchEvent.Kind<*>>): LocalLinuxWatchKey =
            try {
                runBlocking<LocalLinuxWatchKey> {
                    suspendCoroutine { continuation ->
                        post(true, continuation) {
                            try {
                                val pathBytes = path.toByteString()
                                var mask = eventKindsToMask(kinds)
                                mask = maybeAddDontFollowMask(path, mask)
                                val wd = try {
                                    Syscalls.inotify_add_watch(inotifyFd, pathBytes, mask)
                                } catch (e: SyscallException) {
                                    continuation.resumeWithException(
                                        e.toFileSystemException(pathBytes.toString())
                                    )
                                    return@post
                                }
                                val key = LocalLinuxWatchKey(watchService, path, wd)
                                keys[wd] = key
                                continuation.resume(key)
                            } catch (e: RuntimeException) {
                                continuation.resumeWithException(e)
                            }
                        }
                    }
                }
            } catch (e: InterruptedException) {
                throw InterruptedIOException().apply { initCause(e) }
            }

        private fun maybeAddDontFollowMask(path: Path, mask: Int): Int {
            val attributes = try {
                path.readAttributes(BasicFileAttributes::class.java)
            } catch (ignored: IOException) {
                try {
                    path.readAttributes(BasicFileAttributes::class.java, LinkOption.NOFOLLOW_LINKS)
                } catch (ignored: IOException) {
                    null
                }
            }
            return if (attributes != null && attributes.isSymbolicLink) {
                mask or Constants.IN_DONT_FOLLOW
            } else {
                mask
            }
        }

        fun cancel(key: LocalLinuxWatchKey) {
            try {
                runBlocking<Unit> {
                    suspendCoroutine { continuation ->
                        post(true, continuation) {
                            try {
                                if (key.isValid) {
                                    val wd = key.watchDescriptor
                                    try {
                                        Syscalls.inotify_rm_watch(inotifyFd, wd)
                                    } catch (e: SyscallException) {
                                        e.toFileSystemException(key.watchable().toString())
                                            .printStackTrace()
                                    }
                                    key.setInvalid()
                                    keys.remove(wd)
                                }
                                continuation.resume(Unit)
                            } catch (e: RuntimeException) {
                                continuation.resumeWithException(e)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @Throws(IOException::class)
        override fun close() {
            try {
                runBlocking<Unit> {
                    suspendCoroutine { continuation ->
                        post(false, continuation) {
                            try {
                                for (key in keys.values) {
                                    val wd = key.watchDescriptor
                                    try {
                                        Syscalls.inotify_rm_watch(inotifyFd, wd)
                                    } catch (e: SyscallException) {
                                        continuation.resumeWithException(
                                            e.toFileSystemException(key.watchable().toString())
                                        )
                                        return@post
                                    }
                                    key.setInvalid()
                                }
                                keys.clear()
                                try {
                                    Syscalls.close(inotifyFd)
                                    Syscalls.close(socketFds[1])
                                    Syscalls.close(socketFds[0])
                                } catch (e: SyscallException) {
                                    e.printStackTrace()
                                }
                                isClosed = true
                                continuation.resume(Unit)
                            } catch (e: RuntimeException) {
                                continuation.resumeWithException(e)
                            }
                        }
                    }
                }
            } catch (e: InterruptedException) {
                throw InterruptedIOException().apply { initCause(e) }
            }
        }

        private fun post(ensureOpen: Boolean, continuation: Continuation<*>, runnable: () -> Unit) {
            synchronized(lock) {
                runnables.offer {
                    if (isClosed) {
                        if (ensureOpen) {
                            continuation.resumeWithException(ClosedWatchServiceException())
                        }
                        return@offer
                    }
                    runnable()
                }
            }
            try {
                Syscalls.write(socketFds[1], ONE_BYTE)
            } catch (e: InterruptedIOException) {
                continuation.resumeWithException(e)
            } catch (e: SyscallException) {
                continuation.resumeWithException(e.toFileSystemException(null))
            }
        }

        override fun run() {
            val fds = arrayOf(createStructPollFd(socketFds[0]), createStructPollFd(inotifyFd))
            try {
                while (true) {
                    fds[0].revents = 0
                    fds[1].revents = 0
                    Syscalls.poll(fds, -1)
                    if (fds[0].revents.toInt().hasBits(OsConstants.POLLIN)) {
                        val size = try {
                            Syscalls.read(socketFds[0], ONE_BYTE)
                        } catch (e: SyscallException) {
                            if (e.errno != OsConstants.EAGAIN) {
                                throw e
                            }
                            0
                        }
                        if (size > 0) {
                            synchronized(lock) {
                                while (true) {
                                    val runnable = runnables.poll() ?: break
                                    runnable()
                                }
                            }
                            if (isClosed) {
                                break
                            }
                        }
                    }
                    if (fds[1].revents.toInt().hasBits(OsConstants.POLLIN)) {
                        val size = try {
                            Syscalls.read(inotifyFd, inotifyBuffer)
                        } catch (e: SyscallException) {
                            if (e.errno != OsConstants.EAGAIN) {
                                throw e
                            }
                            0
                        }
                        if (size > 0) {
                            if (FileSystemProviders.overflowWatchEvents) {
                                for (key in keys.values) {
                                    key.addEvent(StandardWatchEventKinds.OVERFLOW, null)
                                }
                                continue
                            }
                            val events = Syscalls.inotify_get_events(inotifyBuffer, 0, size)
                            for (event in events) {
                                if (event.mask.hasBits(Constants.IN_Q_OVERFLOW)) {
                                    for (key in keys.values) {
                                        key.addEvent(StandardWatchEventKinds.OVERFLOW, null)
                                    }
                                    break
                                }
                                val key = keys[event.wd]!!
                                if (event.mask.hasBits(Constants.IN_IGNORED)) {
                                    key.setInvalid()
                                    key.signal()
                                    keys.remove(event.wd)
                                } else {
                                    val kind = maskToEventKind(event.mask)
                                    val name = event.name
                                        ?.let { key.watchable().fileSystem.getPath(it) }
                                    key.addEvent(kind, name)
                                }
                            }
                        }
                    }
                }
            } catch (e: InterruptedIOException) {
                e.printStackTrace()
            } catch (e: SyscallException) {
                e.printStackTrace()
            }
        }

        private fun createStructPollFd(fd: FileDescriptor): StructPollfd =
            StructPollfd().apply {
                this.fd = fd
                events = OsConstants.POLLIN.toShort()
            }

        private fun eventKindsToMask(kinds: Set<WatchEvent.Kind<*>>): Int {
            var mask = 0
            for (kind in kinds) {
                when (kind) {
                    StandardWatchEventKinds.ENTRY_CREATE ->
                        mask = mask or (Constants.IN_CREATE or Constants.IN_MOVED_TO)
                    StandardWatchEventKinds.ENTRY_DELETE ->
                        mask = mask or (Constants.IN_DELETE_SELF or Constants.IN_DELETE
                            or Constants.IN_MOVED_FROM)
                    StandardWatchEventKinds.ENTRY_MODIFY ->
                        mask = mask or (Constants.IN_MOVE_SELF or Constants.IN_MODIFY
                            or Constants.IN_ATTRIB)
                }
            }
            return mask
        }

        private fun maskToEventKind(mask: Int): WatchEvent.Kind<Path> =
            when {
                mask.hasBits(Constants.IN_CREATE) || mask.hasBits(Constants.IN_MOVED_TO) ->
                    StandardWatchEventKinds.ENTRY_CREATE
                mask.hasBits(Constants.IN_DELETE_SELF) || mask.hasBits(Constants.IN_DELETE)
                    || mask.hasBits(Constants.IN_MOVED_FROM) ->
                    StandardWatchEventKinds.ENTRY_DELETE
                mask.hasBits(Constants.IN_MOVE_SELF) || mask.hasBits(Constants.IN_MODIFY)
                    || mask.hasBits(Constants.IN_ATTRIB) -> StandardWatchEventKinds.ENTRY_MODIFY
                else -> throw AssertionError(mask)
            }

        companion object {
            private val ONE_BYTE = ByteArray(1)

            private val id = AtomicInteger()
        }
    }
}
