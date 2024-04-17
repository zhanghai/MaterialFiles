/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.ftp.client

import me.zhanghai.android.files.compat.nullInputStream
import me.zhanghai.android.files.provider.common.AbstractFileByteChannel
import me.zhanghai.android.files.provider.common.ByteBufferInputStream
import me.zhanghai.android.files.provider.common.readFully
import org.apache.commons.net.ftp.FTPClient
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

class FileByteChannel(
    private val client: FTPClient,
    private val releaseClient: (FTPClient) -> Unit,
    private val path: String,
    isAppend: Boolean
) : AbstractFileByteChannel(isAppend, joinCancelledRead = true) {
    private val clientLock = Any()

    @Throws(IOException::class)
    override fun onRead(position: Long, size: Int): ByteBuffer {
        val destination = ByteBuffer.allocate(size)
        synchronized(clientLock) {
            client.restartOffset = position
            val inputStream = client.retrieveFileStream(path)
                ?: client.throwNegativeReplyCodeException()
            try {
                val limit = inputStream.use {
                    it.readFully(destination.array(), destination.arrayOffset(), size)
                }
                destination.limit(limit)
            } finally {
                // We will likely close the input stream before the file is fully
                // read and it will result in a false return value here, but that's
                // totally fine.
                client.completePendingCommand()
            }
        }
        return destination
    }

    @Throws(IOException::class)
    override fun onWrite(position: Long, source: ByteBuffer) {
        synchronized(clientLock) {
            client.restartOffset = position
            ByteBufferInputStream(source).use {
                if (!client.storeFile(path, it)) {
                    client.throwNegativeReplyCodeException()
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun onAppend(source: ByteBuffer) {
        synchronized(clientLock) {
            ByteBufferInputStream(source).use {
                if (!client.appendFile(path, it)) {
                    client.throwNegativeReplyCodeException()
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun onTruncate(size: Long) {
        synchronized(clientLock) {
            client.restartOffset = size
            InputStream::class.nullInputStream().use {
                if (!client.storeFile(path, it)) {
                    client.throwNegativeReplyCodeException()
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun onSize(): Long {
        val sizeString = synchronized(clientLock) {
            client.getSize(path) ?: client.throwNegativeReplyCodeException()
        }
        return sizeString.toLongOrNull() ?: throw IOException("Invalid size $sizeString")
    }

    @Throws(IOException::class)
    override fun onClose() {
        synchronized(clientLock) { releaseClient(client) }
    }
}
