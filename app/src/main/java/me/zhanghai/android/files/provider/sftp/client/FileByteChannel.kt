/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.sftp.client

import me.zhanghai.android.files.provider.common.AbstractFileByteChannel
import me.zhanghai.android.files.provider.common.EMPTY
import me.zhanghai.android.files.provider.common.asFuture
import me.zhanghai.android.files.provider.common.map
import me.zhanghai.android.files.util.closeSafe
import me.zhanghai.android.files.util.findCauseByClass
import net.schmizz.sshj.sftp.PacketType
import net.schmizz.sshj.sftp.RemoteFile
import net.schmizz.sshj.sftp.RemoteFileAccessor
import net.schmizz.sshj.sftp.Response
import net.schmizz.sshj.sftp.SFTPException
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.ClosedByInterruptException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

class FileByteChannel(
    private val file: RemoteFile,
    isAppend: Boolean
) : AbstractFileByteChannel(isAppend) {
    override fun onReadAsync(position: Long, size: Int, timeoutMillis: Long): Future<ByteBuffer> =
        try {
            RemoteFileAccessor.asyncRead(file, position, size)
        } catch (e: IOException) {
            throw e.maybeToSpecificException()
        }
            .asFuture()
            .map(
                { response ->
                    val dataLength: Int
                    when (response.type) {
                        PacketType.STATUS -> {
                            response.ensureStatusIs(Response.StatusCode.EOF)
                            return@map ByteBuffer::class.EMPTY
                        }
                        PacketType.DATA -> {
                            dataLength = response.readUInt32AsInt()
                        }
                        else -> throw SFTPException("Unexpected packet type ${response.type}")
                    }
                    if (dataLength == 0) {
                        return@map ByteBuffer::class.EMPTY
                    }
                    val length = dataLength.coerceAtMost(size)
                    ByteBuffer.wrap(response.array(), response.rpos(), length)
                }, { e ->
                    ((e as? ExecutionException)?.cause as? IOException)?.maybeToSpecificException()
                        ?.let { ExecutionException(it) } ?: e
                }
            )

    @Throws(IOException::class)
    override fun onWrite(position: Long, source: ByteBuffer) {
        // I don't think we are using native or read-only ByteBuffer, so just call array() here.
        try {
            file.write(
                position, source.array(), source.arrayOffset() + source.position(),
                source.remaining()
            )
        } catch (e: IOException) {
            throw e.maybeToSpecificException()
        }
        source.position(source.limit())
    }

    @Throws(IOException::class)
    override fun onTruncate(size: Long) {
        try {
            file.setLength(size)
        } catch (e: IOException) {
            throw e.maybeToSpecificException()
        }
    }

    @Throws(IOException::class)
    override fun onSize(): Long =
        try{
            file.length()
        } catch (e: IOException) {
            throw e.maybeToSpecificException()
        }

    private fun IOException.maybeToSpecificException(): IOException =
        when {
            this is SFTPException && statusCode == Response.StatusCode.INVALID_HANDLE -> {
                setClosed()
                AsynchronousCloseException().apply { initCause(this@maybeToSpecificException) }
            }
            findCauseByClass<InterruptedException>() != null -> {
                closeSafe()
                ClosedByInterruptException().apply { initCause(this@maybeToSpecificException) }
            }
            else -> this
        }

    @Throws(IOException::class)
    override fun onClose() {
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
