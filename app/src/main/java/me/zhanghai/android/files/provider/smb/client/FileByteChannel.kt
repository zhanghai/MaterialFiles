/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb.client

import com.hierynomus.mserref.NtStatus
import com.hierynomus.msfscc.fileinformation.FileStandardInformation
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.smbj.common.SMBRuntimeException
import com.hierynomus.smbj.io.ByteChunkProvider
import com.hierynomus.smbj.share.File
import com.hierynomus.smbj.share.FileAccessor
import java8.nio.channels.SeekableByteChannel
import me.zhanghai.android.files.provider.common.ForceableChannel
import me.zhanghai.android.files.util.closeSafe
import java.io.IOException
import java.io.InterruptedIOException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.ClosedByInterruptException
import java.nio.channels.ClosedChannelException

// TODO: Add preemptive-reading or buffering, etc.
class FileByteChannel(
    private val file: File,
    private val isAppend: Boolean
) : ForceableChannel, SeekableByteChannel {
    private var position = 0L
    private val ioLock = Any()

    private var isOpen = true
    private val closeLock = Any()

    @Throws(IOException::class)
    override fun read(destination: ByteBuffer): Int {
        ensureOpen()
        val remaining = destination.remaining()
        if (remaining == 0) {
            return 0
        }
        synchronized(ioLock) {
            val data = try {
                FileAccessor.read(file, position, remaining)
            } catch (e: SMBRuntimeException) {
                throw e.toIOException()
            } ?: return -1
            // Treat 0 data length as EOF, according to
            // com.hierynomus.smbj.share.FileInputStream#loadBuffer().
            if (data.isEmpty()) {
                return -1
            }
            val length = data.size.coerceAtMost(remaining)
            position += length
            destination.put(data, 0, length)
            return length
        }
    }

    @Throws(IOException::class)
    override fun write(source: ByteBuffer): Int {
        ensureOpen()
        synchronized(ioLock) {
            if (isAppend) {
                position = try {
                    file.getFileInformation(FileStandardInformation::class.java).endOfFile
                } catch (e: SMBRuntimeException) {
                    throw e.toIOException()
                }
            }
            if (!source.hasRemaining()) {
                return 0
            }
            val length = try {
                file.write(ByteBufferChunkProvider(source, position))
            } catch (e: SMBRuntimeException) {
                throw e.toIOException()
            }
            position += length
            return length
        }
    }

    override fun position(): Long {
        ensureOpen()
        synchronized(ioLock) {
            return position
        }
    }

    override fun position(newPosition: Long): SeekableByteChannel {
        ensureOpen()
        synchronized(ioLock) {
            position = newPosition
        }
        return this
    }

    @Throws(IOException::class)
    override fun size(): Long {
        ensureOpen()
        return try {
            file.getFileInformation(FileStandardInformation::class.java).endOfFile
        } catch (e: SMBRuntimeException) {
            throw e.toIOException()
        }
    }

    @Throws(IOException::class)
    override fun truncate(size: Long): SeekableByteChannel {
        ensureOpen()
        require(size >= 0)
        val currentSize = try {
            file.getFileInformation(FileStandardInformation::class.java).endOfFile
        } catch (e: SMBRuntimeException) {
            throw e.toIOException()
        }
        if (size >= currentSize) {
            return this
        }
        synchronized(ioLock) {
            try {
                file.setLength(size)
            } catch (e: SMBRuntimeException) {
                throw e.toIOException()
            }
            position = position.coerceAtMost(size)
        }
        return this
    }

    @Throws(IOException::class)
    override fun force(metaData: Boolean) {
        ensureOpen()
        try {
            file.flush()
        } catch (e: SMBRuntimeException) {
            throw e.toIOException()
        }
    }

    @Throws(ClosedChannelException::class)
    private fun ensureOpen() {
        synchronized(closeLock) {
            if (!isOpen) {
                throw ClosedChannelException()
            }
        }
    }

    private fun SMBRuntimeException.toIOException(): IOException =
        when {
            this is SMBApiException && status == NtStatus.STATUS_FILE_CLOSED -> {
                isOpen = false
                AsynchronousCloseException().apply { initCause(this@toIOException) }
            }
            cause?.cause is InterruptedException -> {
                closeSafe()
                ClosedByInterruptException().apply { initCause(this@toIOException) }
            }
            else -> IOException(this)
        }

    override fun isOpen(): Boolean = synchronized(closeLock) { isOpen }

    @Throws(IOException::class)
    override fun close() {
        synchronized(closeLock) {
            if (!isOpen) {
                return
            }
            try {
                file.close()
            } catch (e: SMBRuntimeException) {
                throw when {
                    e.cause?.cause is InterruptedException -> InterruptedIOException()
                        .apply { initCause(e) }
                    else -> IOException(e)
                }
            }
        }
    }
}

private class ByteBufferChunkProvider(
    private val buffer: ByteBuffer,
    offset: Long
) : ByteChunkProvider() {
    init {
        this.offset = offset
    }

    override fun isAvailable(): Boolean = buffer.hasRemaining()

    override fun bytesLeft(): Int = buffer.remaining()

    override fun getChunk(chunk: ByteArray): Int {
        val length = chunk.size.coerceAtMost(buffer.remaining())
        buffer.get(chunk, 0, length)
        return length
    }
}
