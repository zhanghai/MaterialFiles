/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

class ByteBufferInputStream(buffer: ByteBuffer) : InputStream() {
    private var buffer: ByteBuffer? = buffer

    override fun read(): Int {
        val buffer = ensureOpen()
        return if (buffer.hasRemaining()) buffer.get().toInt() and 0xFF else -1
    }

    override fun read(bytes: ByteArray, offset: Int, length: Int): Int {
        val buffer = ensureOpen()
        if (length == 0) {
            return 0
        }
        val remaining = buffer.remaining()
        if (remaining == 0) {
            return -1
        }
        val readLength = length.coerceAtMost(remaining)
        buffer.get(bytes, offset, readLength)
        return readLength
    }

    override fun skip(length: Long): Long {
        val buffer = ensureOpen()
        if (length <= 0) {
            return 0
        }
        val skippedLength = length.toInt().coerceAtMost(buffer.remaining())
        buffer.position(buffer.position() + skippedLength)
        return skippedLength.toLong()
    }

    override fun available(): Int {
        val buffer = ensureOpen()
        return buffer.remaining()
    }

    override fun markSupported(): Boolean = true

    override fun mark(readlimit: Int) {
        val buffer = ensureOpen()
        buffer.mark()
    }

    override fun reset() {
        val buffer = ensureOpen()
        buffer.reset()
    }

    override fun close() {
        buffer = null
    }

    private fun ensureOpen(): ByteBuffer = buffer ?: throw IOException("Stream closed")
}
