/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.ftp.client

import java8.nio.channels.SeekableByteChannel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeout
import me.zhanghai.android.files.provider.common.ForceableChannel
import me.zhanghai.android.files.provider.common.readFully
import me.zhanghai.android.files.util.closeSafe
import org.apache.commons.net.ftp.FTPClient
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.IOException
import java.io.InterruptedIOException
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.NonReadableChannelException

class FileByteChannel(
    private val client: FTPClient,
    private val releaseClient: (FTPClient) -> Unit,
    private val path: String,
    private val isAppend: Boolean
) : ForceableChannel, SeekableByteChannel {
    private val clientLock = Any()

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
        val remaining = source.remaining()
        if (remaining == 0) {
            return 0
        }
        // I don't think we are using native or read-only ByteBuffer, so just call array() here.
        synchronized(ioLock) {
            if (isAppend) {
                synchronized(clientLock) {
                    ByteArrayInputStream(source.array(), source.position(), remaining).use {
                        if (!client.appendFile(path, it)) {
                            client.throwNegativeReplyCodeException()
                        }
                    }
                }
                position = getSize()
            } else {
                synchronized(clientLock) {
                    client.restartOffset = position
                    ByteArrayInputStream(source.array(), source.position(), remaining).use {
                        if (!client.storeFile(path, it)) {
                            client.throwNegativeReplyCodeException()
                        }
                    }
                }
                position += remaining
            }
            source.position(source.limit())
            return remaining
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
            synchronized(clientLock) {
                client.restartOffset = size
                ByteArrayInputStream(byteArrayOf()).use {
                    if (!client.storeFile(path, it)) {
                        client.throwNegativeReplyCodeException()
                    }
                }
            }
            position = position.coerceAtMost(size)
        }
        return this
    }

    @Throws(IOException::class)
    private fun getSize(): Long {
        val sizeString = synchronized(clientLock) {
            client.getSize(path) ?: client.throwNegativeReplyCodeException()
        }
        return sizeString.toLongOrNull() ?: throw IOException("Invalid size $sizeString")
    }

    @Throws(IOException::class)
    override fun force(metaData: Boolean) {
        ensureOpen()
        // Unsupported.
    }

    @Throws(ClosedChannelException::class)
    private fun ensureOpen() {
        synchronized(closeLock) {
            if (!isOpen) {
                throw ClosedChannelException()
            }
        }
    }

    override fun isOpen(): Boolean = synchronized(closeLock) { isOpen }

    @Throws(IOException::class)
    override fun close() {
        synchronized(closeLock) {
            if (!isOpen) {
                return
            }
            isOpen = false
            synchronized(ioLock) {
                readBuffer.closeSafe()
                synchronized(clientLock) { releaseClient(client) }
            }
        }
    }

    private inner class ReadBuffer : Closeable {
        private val bufferSize = DEFAULT_BUFFER_SIZE
        private val timeoutMillis = 15_000L

        private val buffer = ByteBuffer.allocate(bufferSize).apply { limit(0) }
        private var bufferedPosition = 0L

        private var pendingDeferred: Deferred<ByteBuffer>? = null
        private val pendingDeferredLock = Any()

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
            val deferred = synchronized(pendingDeferredLock) {
                pendingDeferred?.also { pendingDeferred = null }
            } ?: readIntoBufferAsync()
            val newBuffer = try {
                runBlocking { deferred.await() }
            } catch (e: CancellationException) {
                throw InterruptedIOException().apply { initCause(e) }
            }
            buffer.clear()
            buffer.put(newBuffer)
            buffer.flip()
            if (!buffer.hasRemaining()) {
                return
            }
            bufferedPosition += buffer.remaining()
            synchronized(pendingDeferredLock) {
                pendingDeferred = readIntoBufferAsync()
            }
        }

        private fun readIntoBufferAsync(): Deferred<ByteBuffer> =
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.async(Dispatchers.IO) {
                withTimeout(timeoutMillis) {
                    runInterruptible {
                        synchronized(clientLock) {
                            client.restartOffset = bufferedPosition
                            val inputStream = client.retrieveFileStream(path)
                                ?: client.throwNegativeReplyCodeException()
                            try {
                                val buffer = ByteBuffer.allocate(bufferSize)
                                val limit = inputStream.use {
                                    it.readFully(
                                        buffer.array(), buffer.position(), buffer.remaining()
                                    )
                                }
                                buffer.limit(limit)
                                buffer
                            } finally {
                                // We will likely close the input stream before the file is fully
                                // read and it will result in a false return value here, but that's
                                // totally fine.
                                client.completePendingCommand()
                            }
                        }
                    }
                }
            }

        fun reposition(oldPosition: Long, newPosition: Long) {
            if (newPosition == oldPosition) {
                return
            }
            val newBufferPosition = buffer.position() + (newPosition - oldPosition)
            if (newBufferPosition in 0..buffer.limit()) {
                buffer.position(newBufferPosition.toInt())
            } else {
                synchronized(pendingDeferredLock) {
                    pendingDeferred?.let {
                        it.cancel()
                        runBlocking { it.join() }
                        pendingDeferred = null
                    }
                }
                buffer.limit(0)
                bufferedPosition = newPosition
            }
        }

        override fun close() {
            synchronized(pendingDeferredLock) {
                pendingDeferred?.let {
                    it.cancel()
                    runBlocking { it.join() }
                    pendingDeferred = null
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 1024 * 1024
    }
}
