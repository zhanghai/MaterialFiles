/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.sftp.client

import me.zhanghai.android.files.util.findCauseByClass
import net.schmizz.concurrent.Promise
import net.schmizz.sshj.sftp.RemoteFile
import net.schmizz.sshj.sftp.RemoteFileAccessor
import net.schmizz.sshj.sftp.Response
import net.schmizz.sshj.sftp.SFTPException
import java.io.IOException
import java.io.InterruptedIOException
import java.io.OutputStream
import java.nio.channels.AsynchronousCloseException
import java.util.ArrayDeque
import java.util.concurrent.TimeUnit

internal class PipelinedFileOutputStream(private val file: RemoteFile) : OutputStream() {
    private val requester = RemoteFileAccessor.getRequester(file)
    private val maxWriteSize =
        requester.subsystem.remoteMaxPacketSize - file.outgoingPacketOverhead
    private val pendingWrites =
        ArrayDeque<Promise<Response, SFTPException>>(MAX_PENDING_WRITES)
    private val singleByte = ByteArray(1)

    private var fileOffset = 0L
    private var failure: IOException? = null
    private var isClosed = false

    init {
        check(maxWriteSize > 0)
    }

    @Throws(IOException::class)
    override fun write(value: Int) {
        singleByte[0] = value.toByte()
        write(singleByte, 0, 1)
    }

    @Throws(IOException::class)
    override fun write(buffer: ByteArray, offset: Int, length: Int) {
        ensureOpen()
        if (offset < 0 || length < 0 || offset > buffer.size - length) {
            throw IndexOutOfBoundsException()
        }
        failure?.let { throw it }
        var currentOffset = offset
        var remaining = length
        while (remaining > 0) {
            if (pendingWrites.size == MAX_PENDING_WRITES) {
                awaitNextWrite()
                failure?.let { throw it }
            }
            val writeSize = remaining.coerceAtMost(maxWriteSize)
            val promise = try {
                RemoteFileAccessor.asyncWrite(
                    file, fileOffset, buffer, currentOffset, writeSize
                )
            } catch (e: IOException) {
                recordFailure(e)
                throw failure!!
            }
            pendingWrites.addLast(promise)
            fileOffset += writeSize
            currentOffset += writeSize
            remaining -= writeSize
        }
    }

    @Throws(IOException::class)
    override fun flush() {
        ensureOpen()
        drainPendingWrites()
        failure?.let { throw it }
    }

    @Throws(IOException::class)
    override fun close() {
        if (isClosed) {
            return
        }
        isClosed = true
        drainPendingWrites()
        try {
            file.close()
        } catch (e: SFTPException) {
            if (e.statusCode != Response.StatusCode.NO_SUCH_FILE) {
                recordFailure(e)
            }
        } catch (e: IOException) {
            recordFailure(e)
        }
        failure?.let { throw it }
    }

    private fun drainPendingWrites() {
        while (pendingWrites.isNotEmpty()) {
            awaitNextWrite()
        }
    }

    private fun awaitNextWrite() {
        val promise = pendingWrites.removeFirst()
        try {
            promise.retrieve(requester.timeoutMs.toLong(), TimeUnit.MILLISECONDS)
                .ensureStatusPacketIsOK()
        } catch (e: IOException) {
            recordFailure(e)
        }
    }

    private fun recordFailure(exception: IOException) {
        val exception = exception.toStreamException()
        val failure = failure
        if (failure == null) {
            this.failure = exception
        } else if (failure !== exception) {
            failure.addSuppressed(exception)
        }
    }

    private fun IOException.toStreamException(): IOException =
        when {
            this is SFTPException && statusCode == Response.StatusCode.INVALID_HANDLE ->
                AsynchronousCloseException().apply { initCause(this@toStreamException) }
            findCauseByClass<InterruptedException>() != null -> {
                Thread.interrupted()
                InterruptedIOException().apply { initCause(this@toStreamException) }
            }
            else -> this
        }

    @Throws(IOException::class)
    private fun ensureOpen() {
        if (isClosed) {
            throw IOException("Stream closed")
        }
    }

    companion object {
        private const val MAX_PENDING_WRITES = 8
    }
}
