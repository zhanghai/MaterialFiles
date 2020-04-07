/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import android.os.Parcelable
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.min

@Parcelize
class ByteString private constructor(
    private val bytes: ByteArray
) : Comparable<ByteString>, Parcelable {
    val length: Int
        get() = bytes.size

    operator fun get(index: Int): Byte = bytes[index]

    operator fun iterator(): ByteIterator = bytes.iterator()

    val indices: IntRange
        get() = bytes.indices

    fun isEmpty(): Boolean = bytes.isEmpty()

    fun isNotEmpty(): Boolean = bytes.isNotEmpty()

    fun borrowBytes(): ByteArray = bytes

    fun toBytes(): ByteArray = bytes.copyOf()

    fun startsWith(prefix: ByteString, startIndex: Int = 0): Boolean {
        if (startIndex !in 0..length - prefix.length) {
            return false
        }
        for (index in prefix.indices) {
            if (this[startIndex + index] != prefix[index]) {
                return false
            }
        }
        return true
    }

    fun endsWith(suffix: ByteString): Boolean = startsWith(suffix, length - suffix.length)

    fun indexOf(byte: Byte, fromIndex: Int = 0): Int {
        for (index in fromIndex.coerceAtLeast(0) until length) {
            if (this[index] == byte) {
                return index
            }
        }
        return -1
    }

    fun lastIndexOf(byte: Byte, fromIndex: Int = length - 1): Int {
        for (index in fromIndex.coerceAtMost(length - 1) downTo 0) {
            if (this[index] == byte) {
                return index
            }
        }
        return -1
    }

    fun contains(byte: Byte): Boolean = indexOf(byte) != -1

    fun indexOf(substring: ByteString, fromIndex: Int = 0): Int {
        for (index in fromIndex.coerceAtLeast(0) until length - substring.length) {
            if (startsWith(substring, index)) {
                return index
            }
        }
        return -1
    }

    fun lastIndexOf(substring: ByteString): Int = lastIndexOf(substring, length - substring.length)

    fun lastIndexOf(substring: ByteString, fromIndex: Int): Int {
        for (index in fromIndex.coerceAtMost(length - substring.length) downTo 0) {
            if (startsWith(substring, index)) {
                return index
            }
        }
        return -1
    }

    fun contains(substring: ByteString): Boolean = indexOf(substring) != -1

    fun substring(start: Int, end: Int = length): ByteString {
        val length = length
        if (start < 0 || end > length || start > end) {
            throw IndexOutOfBoundsException()
        }
        if (start == 0 && end == length) {
            return this
        }
        return ByteString(bytes.copyOfRange(start, end))
    }

    fun substring(range: IntRange): ByteString = substring(range.first, range.last + 1)

    operator fun plus(other: ByteString): ByteString {
        if (other.isEmpty()) {
            return this
        }
        return ByteString(bytes + other.bytes)
    }

    fun split(delimiter: ByteString): List<ByteString> {
        require(delimiter.isNotEmpty())
        val result = mutableListOf<ByteString>()
        var start = 0
        while (true) {
            val end = indexOf(delimiter, start)
            if (end == -1) {
                break
            }
            result.add(substring(start, end))
            start = end + delimiter.length
        }
        result.add(substring(start))
        return result
    }

    @IgnoredOnParcel
    private var stringCache: String? = null

    override fun toString(): String {
        // We are okay with the potential race condition here.
        var string = stringCache
        if (string == null) {
            // String() uses replacement char instead of throwing exception.
            string = String(bytes)
            stringCache = string
        }
        return string
    }

    val cstr: ByteArray
        get() = bytes + '\u0000'.toByte()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as ByteString
        return bytes contentEquals other.bytes
    }

    override fun hashCode(): Int = bytes.contentHashCode()

    override fun compareTo(other: ByteString): Int = bytes.compareTo(other.bytes)

    private fun ByteArray.compareTo(other: ByteArray): Int {
        val size = size
        val otherSize = other.size
        for (index in 0 until min(size, otherSize)) {
            val byte = this[index]
            val otherByte = other[index]
            val result = byte - otherByte
            if (result != 0) {
                return result
            }
        }
        return size - otherSize
    }

    companion object {
        val EMPTY = ByteString(ByteArray(0))

        fun fromBytes(bytes: ByteArray, start: Int = 0, end: Int = bytes.size): ByteString =
            ByteString(bytes.copyOfRange(start, end))

        fun takeBytes(bytes: ByteArray): ByteString = ByteString(bytes)

        fun fromString(string: String): ByteString =
            ByteString(string.toByteArray()).apply { stringCache = string }
    }
}

fun Byte.toByteString(): ByteString = ByteString.takeBytes(byteArrayOf(this))

fun ByteArray.toByteString(start: Int = 0, end: Int = size): ByteString =
    ByteString.fromBytes(this, start, end)

fun ByteArray.moveToByteString(): ByteString = ByteString.takeBytes(this)

fun String.toByteString(): ByteString = ByteString.fromString(this)

@OptIn(ExperimentalContracts::class)
fun ByteString?.isNullOrEmpty(): Boolean {
    contract { returns(false) implies (this@isNullOrEmpty != null) }
    return this == null || this.isEmpty()
}
