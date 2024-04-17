/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties.checksum

import java.security.MessageDigest
import java.util.zip.CRC32

class Crc32MessageDigest : MessageDigest("CRC32") {
    private val crc32 = CRC32()

    override fun engineUpdate(input: Byte) {
        crc32.update(input.toInt())
    }

    override fun engineUpdate(input: ByteArray, offset: Int, length: Int) {
        crc32.update(input, offset, length)
    }

    override fun engineDigest(): ByteArray {
        val value = crc32.value
        crc32.reset()
        return ByteArray(4).apply {
            this[0] = (value ushr 24).toByte()
            this[1] = (value ushr 16).toByte()
            this[2] = (value ushr 8).toByte()
            this[3] = value.toByte()
        }
    }

    override fun engineReset() {
        crc32.reset()
    }
}