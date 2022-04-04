/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb.client

import com.hierynomus.mserref.NtStatus
import com.hierynomus.msfscc.fileinformation.FileStandardInformation
import com.hierynomus.mssmb2.SMBApiException
import com.hierynomus.mssmb2.messages.SMB2ReadResponse
import com.hierynomus.protocol.commons.concurrent.Futures
import com.hierynomus.protocol.transport.TransportException
import com.hierynomus.smbj.common.SMBRuntimeException
import com.hierynomus.smbj.io.ByteChunkProvider
import com.hierynomus.smbj.share.File
import com.hierynomus.smbj.share.FileAccessor
import java8.nio.channels.SeekableByteChannel
import me.zhanghai.android.files.provider.common.ForceableChannel
import me.zhanghai.android.files.util.closeSafe
import me.zhanghai.android.files.util.findCauseByClass
import java.io.Closeable
import java.io.IOException
import java.io.InterruptedIOException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.ClosedByInterruptException
import java.nio.channels.ClosedChannelException
import java.nio.channels.NonReadableChannelException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class FileByteChannel(
    private val file: File,
    private val isAppend: Boolean
) : ForceableChannel, SeekableByteChannel {
    private var position = 0L
    private val readBuffer = ReadBuffer()
    private val ioLock = Any()

    private var isOpen = true
    private val closeLock = Any()

    @Throws(IOException::class)
    override fun read(destination: ByteBuffer): Int {
        ensureOpen()
        if (isAppend) {
            throw NonReadableChannelException()
        }
        val remaining = destination.remaining()
        if (remaining == 0) {
            return 0
        }
        return synchronized(ioLock) {
            readBuffer.read(destination).also {
                if (it != -1) {
                    position += it
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun write(source: ByteBuffer): Int {
        ensureOpen()
        if (!source.hasRemaining()) {
            return 0
        }
        synchronized(ioLock) {
            if (isAppend) {
                position = getSize()
            }
            return try {
                file.write(ByteBufferChunkProvider(source, position))
            } catch (e: SMBRuntimeException) {
                throw e.toIOException()
            }.also {
                position += it
            }
        }
    }

    @Throws(IOException::class)
    override fun position(): Long {
        ensureOpen()
        synchronized(ioLock) {
            if (isAppend) {
                position = getSize()
            }
            return position
        }
    }

    override fun position(newPosition: Long): SeekableByteChannel {
        ensureOpen()
        if (isAppend) {
            // Ignored.
            return this
        }
        synchronized(ioLock) {
            readBuffer.reposition(position, newPosition)
            position = newPosition
        }
        return this
    }

    @Throws(IOException::class)
    override fun size(): Long {
        ensureOpen()
        return getSize()
    }

    @Throws(IOException::class)
    override fun truncate(size: Long): SeekableByteChannel {
        ensureOpen()
        require(size >= 0)
        synchronized(ioLock) {
            val currentSize = getSize()
            if (size >= currentSize) {
                return this
            }
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
    private fun getSize(): Long =
        try {
            file.getFileInformation(FileStandardInformation::class.java).endOfFile
        } catch (e: SMBRuntimeException) {
            throw e.toIOException()
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
            findCauseByClass<SMBApiException>()
                .let { it != null && it.status == NtStatus.STATUS_FILE_CLOSED } -> {
                synchronized(closeLock) { isOpen = false }
                AsynchronousCloseException().apply { initCause(this@toIOException) }
            }
            findCauseByClass<InterruptedException>() != null -> {
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
            isOpen = false
            readBuffer.closeSafe()
            try {
                file.close()
            } catch (e: SMBRuntimeException) {
                throw when {
                    e.findCauseByClass<InterruptedException>() != null ->
                        InterruptedIOException().apply { initCause(e) }
                    else -> IOException(e)
                }
            }
        }
    }

    private inner class ReadBuffer : Closeable {
        private val bufferSize: Int
        private val timeout: Long

        init {
            val treeConnect = file.diskShare.treeConnect
            val config = treeConnect.config
            bufferSize = config.readBufferSize
                .coerceAtMost(treeConnect.session.connection.negotiatedProtocol.maxReadSize)
            timeout = config.readTimeout
        }

        private val buffer = ByteBuffer.allocate(bufferSize).apply { limit(0) }
        private var bufferedPosition = 0L

        private var pendingFuture: Future<SMB2ReadResponse>? = null
        private val pendingFutureLock = Any()

        @Throws(IOException::class)
        fun read(destination: ByteBuffer): Int {
            if (!buffer.hasRemaining()) {
                readIntoBuffer()
                if (!buffer.hasRemaining()) {
                    return -1
                }
            }
            val length = destination.remaining().coerceAtMost(buffer.remaining())
            val bufferLimit = buffer.limit()
            buffer.limit(buffer.position() + length)
            destination.put(buffer)
            buffer.limit(bufferLimit)
            return length
        }

        @Throws(IOException::class)
        private fun readIntoBuffer() {
            val future = synchronized(pendingFutureLock) {
                pendingFuture?.also { pendingFuture = null }
            } ?: readIntoBufferAsync()
            val response = try {
                receive(future, timeout)
            } catch (e: SMBRuntimeException) {
                throw e.toIOException()
            }
            when (response.header.statusCode) {
                NtStatus.STATUS_END_OF_FILE.value -> {
                    buffer.limit(0)
                    return
                }
                NtStatus.STATUS_SUCCESS.value -> {}
                else -> throw SMBApiException(response.header, "Read failed for $this")
                    .toIOException()
            }
            val data = response.data
            if (data.isEmpty()) {
                buffer.limit(0)
                return
            }
            buffer.clear()
            val length = data.size.coerceAtMost(buffer.remaining())
            buffer.put(data, 0, length)
            buffer.flip()
            bufferedPosition += length
            synchronized(pendingFutureLock) {
                try {
                    pendingFuture = readIntoBufferAsync()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        // @see com.hierynomus.smbj.share.Share.receive
        @Throws(SMBRuntimeException::class)
        private fun <T> receive(future: Future<T>, timeout: Long): T =
            try {
                Futures.get(future, timeout, TimeUnit.MILLISECONDS, TransportException.Wrapper)
            } catch (e: TransportException) {
                throw SMBRuntimeException(e)
            }

        @Throws(IOException::class)
        private fun readIntoBufferAsync(): Future<SMB2ReadResponse> =
            try {
                FileAccessor.readAsync(file, bufferedPosition, bufferSize)
            } catch (e: SMBRuntimeException) {
                throw e.toIOException()
            }

        fun reposition(oldPosition: Long, newPosition: Long) {
            if (newPosition == oldPosition) {
                return
            }
            val newBufferPosition = buffer.position() + (newPosition - oldPosition)
            if (newBufferPosition in 0..buffer.limit()) {
                buffer.position(newBufferPosition.toInt())
            } else {
                synchronized(pendingFutureLock) {
                    // TransportException: Received response with unknown sequence number
                    //pendingFuture?.cancel(true)?.also { pendingFuture = null }
                    pendingFuture = null
                }
                buffer.limit(0)
                bufferedPosition = newPosition
            }
        }

        override fun close() {
            synchronized(pendingFutureLock) {
                // TransportException: Received response with unknown sequence number
                //pendingFuture?.cancel(true)?.also { pendingFuture = null }
                pendingFuture = null
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

    override fun prepareWrite(maxBytesToPrepare: Int) {}

    override fun getChunk(chunk: ByteArray): Int {
        val length = chunk.size.coerceAtMost(buffer.remaining())
        buffer.get(chunk, 0, length)
        return length
    }
}
