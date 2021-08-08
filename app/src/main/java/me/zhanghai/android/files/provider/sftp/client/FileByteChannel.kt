/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.sftp.client

import java8.nio.channels.SeekableByteChannel
import me.zhanghai.android.files.provider.common.ForceableChannel
import me.zhanghai.android.files.util.closeSafe
import net.schmizz.concurrent.Promise
import net.schmizz.sshj.sftp.PacketType
import net.schmizz.sshj.sftp.RemoteFile
import net.schmizz.sshj.sftp.RemoteFileAccessor
import net.schmizz.sshj.sftp.Response
import net.schmizz.sshj.sftp.SFTPException
import java.io.IOException
import java.io.InterruptedIOException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.ClosedByInterruptException
import java.nio.channels.ClosedChannelException
import java.nio.channels.NonReadableChannelException
import java.util.concurrent.TimeUnit

class FileByteChannel(
    private val file: RemoteFile,
    private val isAppend: Boolean
) : ForceableChannel, SeekableByteChannel {
    private var position = 0L
    private val ioLock = Any()

    private val readBuffer = ReadBuffer()

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
        synchronized(ioLock) {
            if (isAppend) {
                position = getSize()
            }
            // I don't think we are using native or read-only ByteBuffer, so just call array() here.
            try {
                file.write(position, source.array(), source.position(), remaining)
            } catch (e: IOException) {
                throw e.maybeToSpecificException()
            }
            source.position(source.limit())
            position += remaining
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
            try {
                file.setLength(size)
            } catch (e: IOException) {
                throw e.maybeToSpecificException()
            }
            position = position.coerceAtMost(size)
        }
        return this
    }

    @Throws(IOException::class)
    private fun getSize(): Long =
        try{
            file.length()
        } catch (e: IOException) {
            throw e.maybeToSpecificException()
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

    private fun IOException.maybeToSpecificException(): IOException =
        when {
            this is SFTPException && statusCode == Response.StatusCode.INVALID_HANDLE -> {
                synchronized(closeLock) { isOpen = false }
                AsynchronousCloseException().apply { initCause(this@maybeToSpecificException) }
            }
            this is InterruptedIOException || cause is InterruptedException -> {
                closeSafe()
                ClosedByInterruptException().apply { initCause(this@maybeToSpecificException) }
            }
            else -> this
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
            } catch (e: SFTPException) {
                // NO_SUCH_FILE is returned when canceling an in-progress copy to SFTP server.
                if (e.statusCode != Response.StatusCode.NO_SUCH_FILE) {
                    throw e
                }
            }
        }
    }

    private inner class ReadBuffer {
        private val bufferSize: Int
        private val timeout: Long

        init {
            val engine = RemoteFileAccessor.getRequester(file)
            bufferSize = DEFAULT_BUFFER_SIZE
            timeout = engine.timeoutMs.toLong()
        }

        private val buffer = ByteBuffer.allocate(bufferSize).apply { limit(0) }
        private var bufferedPosition = 0L

        private var pendingPromise: Promise<Response, SFTPException>? = null
        private val pendingPromiseLock = Any()

        @Throws(IOException::class)
        fun read(destination: ByteBuffer): Int {
            if (!buffer.hasRemaining()) {
                if (!readIntoBuffer()) {
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
        private fun readIntoBuffer(): Boolean {
            val promise = synchronized(pendingPromiseLock) {
                pendingPromise?.also { pendingPromise = null }
            } ?: readIntoBufferAsync()
            val response = try {
                promise.retrieve(timeout, TimeUnit.MILLISECONDS)
            } catch (e: IOException) {
                throw e.maybeToSpecificException()
            }
            val dataLength: Int
            when (response.type) {
                PacketType.STATUS -> {
                    response.ensureStatusIs(Response.StatusCode.EOF)
                    return false
                }
                PacketType.DATA -> {
                    dataLength = response.readUInt32AsInt()
                }
                else -> throw SFTPException("Unexpected packet type ${response.type}")
            }
            if (dataLength == 0) {
                return false
            }
            buffer.clear()
            val length = dataLength.coerceAtMost(buffer.remaining())
            buffer.put(response.array(), response.rpos(), length)
            buffer.flip()
            bufferedPosition += length
            synchronized(pendingPromiseLock) {
                pendingPromise = try {
                    readIntoBufferAsync()
                } catch (e: IOException) {
                    e.printStackTrace()
                    null
                }
            }
            return true
        }

        @Throws(IOException::class)
        private fun readIntoBufferAsync(): Promise<Response, SFTPException> =
            try {
                RemoteFileAccessor.asyncRead(file, bufferedPosition, bufferSize)
            } catch (e: IOException) {
                throw e.maybeToSpecificException()
            }

        fun reposition(oldPosition: Long, newPosition: Long) {
            if (newPosition == oldPosition) {
                return
            }
            val newBufferPosition = buffer.position() + (newPosition - oldPosition)
            if (newBufferPosition in 0..buffer.limit()) {
                buffer.position(newBufferPosition.toInt())
            } else {
                synchronized(pendingPromiseLock) {
                    pendingPromise = null
                }
                buffer.limit(0)
                bufferedPosition = newPosition
            }
        }
    }

    companion object {
        // @see SmbConfig.DEFAULT_BUFFER_SIZE
        private const val DEFAULT_BUFFER_SIZE = 1024 * 1024
    }
}
