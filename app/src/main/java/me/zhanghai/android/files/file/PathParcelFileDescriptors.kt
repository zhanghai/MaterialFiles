/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file

import android.os.Handler
import android.os.ParcelFileDescriptor
import android.os.StrictMode
import android.system.ErrnoException
import android.system.OsConstants
import java8.nio.channels.SeekableByteChannel
import java8.nio.file.AccessDeniedException
import java8.nio.file.FileSystemException
import java8.nio.file.FileSystemLoopException
import java8.nio.file.NoSuchFileException
import java8.nio.file.OpenOption
import java8.nio.file.Path
import java8.nio.file.StandardOpenOption
import me.zhanghai.android.files.app.storageManager
import me.zhanghai.android.files.compat.ProxyFileDescriptorCallbackCompat
import me.zhanghai.android.files.compat.openProxyFileDescriptorCompat
import me.zhanghai.android.files.provider.common.InvalidFileNameException
import me.zhanghai.android.files.provider.common.IsDirectoryException
import me.zhanghai.android.files.provider.common.force
import me.zhanghai.android.files.provider.common.isForceable
import me.zhanghai.android.files.provider.common.newByteChannel
import me.zhanghai.android.files.provider.linux.isLinuxPath
import me.zhanghai.android.files.provider.linux.syscall.SyscallException
import me.zhanghai.android.files.util.hasBits
import me.zhanghai.android.files.util.withoutPenaltyDeathOnNetwork
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InterruptedIOException
import java.nio.ByteBuffer
import java.nio.channels.ClosedByInterruptException

/**
 * Opens this path as a [ParcelFileDescriptor] for handing to another app, using a proxy file
 * descriptor backed by [callbackHandler] when the file can't be opened directly (e.g. network,
 * archive or root-only files).
 *
 * Extracted from [FileProvider] so that it can be shared with
 * [me.zhanghai.android.files.documentsprovider.FilesDocumentsProvider].
 */
@Throws(FileNotFoundException::class)
fun Path.openParcelFileDescriptor(mode: String, callbackHandler: Handler): ParcelFileDescriptor {
    val path = this
    val modeBits = ParcelFileDescriptor.parseMode(mode)
    if (path.canOpenDirectly(modeBits)) {
        return ParcelFileDescriptor.open(path.toFile(), modeBits)
    }
    val options = modeBits.toOpenOptions()
    val channel = try {
        // Strict mode thread policy is passed through binder, but some apps (notably music
        // players) like to open file on their main thread.
        StrictMode::class.withoutPenaltyDeathOnNetwork {
            path.newByteChannel(options)
        }
    } catch (e: IOException) {
        throw e.toFileNotFoundException()
    }
    return try {
        storageManager.openProxyFileDescriptorCompat(
            modeBits, ChannelCallback(channel), callbackHandler
        )
    } catch (e: IOException) {
        throw e.toFileNotFoundException()
    }
}

private fun Path.canOpenDirectly(mode: Int): Boolean {
    if (!isLinuxPath) {
        return false
    }
    val file = toFile()
    val readOnly = mode.hasBits(ParcelFileDescriptor.MODE_READ_ONLY)
    val writeOnly = mode.hasBits(ParcelFileDescriptor.MODE_WRITE_ONLY)
    val readWrite = mode.hasBits(ParcelFileDescriptor.MODE_READ_WRITE)
    val needRead = readOnly || readWrite
    val needWrite = writeOnly || readWrite
    return !((needRead && !file.canRead()) || (needWrite && !file.canWrite()))
}

private fun Int.toOpenOptions(): Set<OpenOption> =
    mutableSetOf<OpenOption>().apply {
        // May be "r" for read-only access, "rw" for read and write access, or "rwt" for
        // read and write access that truncates any existing file.
        require(!hasBits(ParcelFileDescriptor.MODE_APPEND)) { "mode ${this@toOpenOptions}" }
        if (hasBits(ParcelFileDescriptor.MODE_READ_ONLY)
            || hasBits(ParcelFileDescriptor.MODE_READ_WRITE)) {
            this += StandardOpenOption.READ
        }
        if (hasBits(ParcelFileDescriptor.MODE_WRITE_ONLY)
            || hasBits(ParcelFileDescriptor.MODE_READ_WRITE)) {
            this += StandardOpenOption.WRITE
        }
        if (hasBits(ParcelFileDescriptor.MODE_CREATE)) {
            this += StandardOpenOption.CREATE
        }
        if (hasBits(ParcelFileDescriptor.MODE_TRUNCATE)) {
            this += StandardOpenOption.TRUNCATE_EXISTING
        }
    }

private fun IOException.toFileNotFoundException(): FileNotFoundException =
    if (this is FileNotFoundException) {
        this
    } else {
        FileNotFoundException(message).apply { initCause(this@toFileNotFoundException) }
    }

private class ChannelCallback(
    private val channel: SeekableByteChannel
) : ProxyFileDescriptorCallbackCompat() {
    private var offset = 0L
    private var released = false

    @Throws(ErrnoException::class)
    override fun onGetSize(): Long {
        ensureNotReleased()
        return try {
            channel.size()
        } catch (e: IOException) {
            throw e.toErrnoException()
        }
    }

    @Throws(ErrnoException::class)
    override fun onRead(offset: Long, size: Int, data: ByteArray): Int {
        ensureNotReleased()
        if (this.offset != offset) {
            try {
                channel.position(offset)
            } catch (e: IOException) {
                throw e.toErrnoException()
            }
            this.offset = offset
        }
        val buffer = ByteBuffer.wrap(data, 0, size)
        // Unlike ReadableByteChannel which may not fill the buffer and returns -1 upon
        // end-of-stream, we need to read as much as we can unless end-of-stream is reached.
        while (buffer.hasRemaining()) {
            val channelSize = try {
                channel.read(buffer)
            } catch (e: IOException) {
                throw e.toErrnoException()
            }
            if (channelSize == -1) {
                break
            }
            this.offset += channelSize
        }
        return (this.offset - offset).toInt()
    }

    @Throws(ErrnoException::class)
    override fun onWrite(offset: Long, size: Int, data: ByteArray): Int {
        ensureNotReleased()
        if (this.offset != offset) {
            try {
                channel.position(offset)
            } catch (e: IOException) {
                throw e.toErrnoException()
            }
            this.offset = offset
        }
        val buffer = ByteBuffer.wrap(data, 0, size)
        return try {
            channel.write(buffer)
        } catch (e: IOException) {
            throw e.toErrnoException()
        }.also { this.offset += it.toLong() }
    }

    @Throws(ErrnoException::class)
    override fun onFsync() {
        ensureNotReleased()
        if (channel.isForceable) {
            try {
                channel.force(true)
            } catch (e: IOException) {
                throw e.toErrnoException()
            }
        }
    }

    @Throws(ErrnoException::class)
    private fun ensureNotReleased() {
        if (released) {
            throw ErrnoException(null, OsConstants.EBADF)
        }
    }

    override fun onRelease() {
        if (released) {
            return
        }
        try {
            channel.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        released = true
    }

    private fun IOException.toErrnoException(): ErrnoException {
        val cause = cause
        return if (this is FileSystemException && cause is SyscallException) {
            ErrnoException(cause.functionName, cause.errno, this)
        } else {
            val errno = when (this) {
                is AccessDeniedException -> OsConstants.EPERM
                is FileSystemLoopException -> OsConstants.ELOOP
                is InvalidFileNameException -> OsConstants.EINVAL
                is IsDirectoryException -> OsConstants.EISDIR
                is NoSuchFileException -> OsConstants.ENOENT
                is ClosedByInterruptException, is InterruptedIOException -> OsConstants.EINTR
                else -> OsConstants.EIO
            }
            ErrnoException(message, errno, this)
        }
    }
}
