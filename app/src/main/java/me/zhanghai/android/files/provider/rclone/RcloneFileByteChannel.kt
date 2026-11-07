/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.rclone

import java8.nio.file.NoSuchFileException
import me.zhanghai.android.files.provider.common.AbstractFileByteChannel
import me.zhanghai.android.files.provider.common.EMPTY
import me.zhanghai.android.files.provider.common.readFully
import me.zhanghai.android.files.provider.rclone.client.RcloneClient
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

internal class RcloneFileByteChannel(
    private val path: RclonePath,
    isAppend: Boolean
) : AbstractFileByteChannel(isAppend) {
    private var tempFile: File? = null
    private var tempFileOutputStream: FileOutputStream? = null

    @Throws(IOException::class)
    override fun onRead(position: Long, size: Int): ByteBuffer {
        val inputStream = try {
            RcloneClient.downloadRange(path.remoteName, path.remotePath, position, size)
        } catch (e: Exception) {
            if (e.message?.contains("416") == true) {
                return ByteBuffer::class.EMPTY
            }
            throw if (e is IOException) e else IOException(e)
        }
        val destination = ByteBuffer.allocate(size)
        val limit = inputStream.use {
            it.readFully(destination.array(), destination.arrayOffset(), size)
        }
        destination.limit(limit)
        return destination
    }

    @Throws(IOException::class)
    override fun onWrite(position: Long, source: ByteBuffer) {
        val outputStream = tempFileOutputStream ?: run {
            val file = File.createTempFile("rclone_write", null)
            tempFile = file
            FileOutputStream(file).also { tempFileOutputStream = it }
        }
        val remaining = source.remaining()
        outputStream.write(source.array(), source.arrayOffset() + source.position(), remaining)
    }

    @Throws(IOException::class)
    override fun onTruncate(size: Long) {
        if (size != 0L) {
            throw IOException("Unsupported truncate to non-zero size")
        }
        tempFileOutputStream?.close()
        val file = File.createTempFile("rclone_write", null)
        tempFile = file
        tempFileOutputStream = FileOutputStream(file)
    }

    @Throws(IOException::class)
    override fun onSize(): Long {
        val item = RcloneClient.stat(path.remoteName, path.remotePath)
            ?: throw NoSuchFileException(path.toString())
        return item.size.coerceAtLeast(0L)
    }

    @Throws(IOException::class)
    override fun onClose() {
        tempFileOutputStream?.let {
            it.close()
            tempFileOutputStream = null
            tempFile?.let { file ->
                try {
                    RcloneClient.uploadFile(path.remoteName, path.remotePath, file)
                } finally {
                    file.delete()
                    tempFile = null
                }
            }
        }
    }
}
