package me.zhanghai.kotlin.filesystem.internal

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.indexOf

internal inline operator fun ByteString.contains(byte: Byte): Boolean = indexOf(byte) != -1

internal inline fun ByteString.first(): Byte = this[0]

internal inline fun ByteString.last(): Byte = this[lastIndex]

internal val ByteString.lastIndex: Int
    inline get() = size - 1
