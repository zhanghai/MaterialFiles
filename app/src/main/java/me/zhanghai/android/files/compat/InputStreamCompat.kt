/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import java.io.IOException
import java.io.InputStream
import kotlin.reflect.KClass

fun KClass<InputStream>.nullInputStream(): InputStream =
    object : InputStream() {
        private var closed = false

        override fun read(): Int {
            ensureOpen()
            return -1
        }

        override fun read(bytes: ByteArray, offset: Int, length: Int): Int {
            if (!(offset >= 0 && length >= 0 && length <= bytes.size - offset)) {
                throw IndexOutOfBoundsException()
            }
            ensureOpen()
            return if (length == 0) 0 else -1
        }

        override fun skip(length: Long): Long {
            ensureOpen()
            return 0
        }

        override fun available(): Int {
            ensureOpen()
            return 0
        }

        override fun close() {
            closed = true
        }

        private fun ensureOpen() {
            if (closed) {
                throw IOException("Stream closed")
            }
        }
    }
