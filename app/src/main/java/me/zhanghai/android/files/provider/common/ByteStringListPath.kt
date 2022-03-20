/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.InvalidPathException
import java8.nio.file.Path
import java8.nio.file.ProviderMismatchException
import me.zhanghai.android.files.compat.readBooleanCompat
import me.zhanghai.android.files.compat.writeBooleanCompat
import me.zhanghai.android.files.compat.writeParcelableListCompat
import me.zhanghai.android.files.util.endsWith
import me.zhanghai.android.files.util.hash
import me.zhanghai.android.files.util.readParcelableListCompat
import me.zhanghai.android.files.util.startsWith
import java.net.URI
import kotlin.math.min

abstract class ByteStringListPath<T : ByteStringListPath<T>> : AbstractPath<T>, Parcelable {
    protected val separator: Byte
    private val isAbsolute: Boolean
    private val segments: List<ByteString>

    @Volatile
    private var byteStringCache: ByteString? = null

    constructor(separator: Byte, path: ByteString) {
        require(separator != '\u0000'.code.toByte()) { "Separator cannot be the nul character" }
        this.separator = separator
        if (path.contains('\u0000'.code.toByte())) {
            throw InvalidPathException(path.toString(), "Path cannot contain nul characters")
        }
        isAbsolute = isPathAbsolute(path)
        val segments = mutableListOf<ByteString>()
        if (path.isEmpty()) {
            segments += ByteString.EMPTY
        } else {
            var start = 0
            val length = path.length
            while (start < length) {
                while (start < length && path[start] == separator) {
                    ++start
                }
                if (start == length) {
                    break
                }
                var end = start + 1
                while (end < length && path[end] != separator) {
                    ++end
                }
                segments += path.substring(start, end)
                start = end
            }
        }
        this.segments = segments
        checkIsAbsoluteOrNotEmpty()
    }

    protected constructor(separator: Byte, isAbsolute: Boolean, segments: List<ByteString>) {
        this.separator = separator
        this.isAbsolute = isAbsolute
        this.segments = segments
        checkIsAbsoluteOrNotEmpty()
    }

    private fun checkIsAbsoluteOrNotEmpty() {
        check(isAbsolute || segments.isNotEmpty()) { "Non-absolute path must not be empty" }
    }

    override fun isAbsolute(): Boolean = isAbsolute

    val fileNameByteString: ByteString?
        get() = segments.lastOrNull()

    override fun getNameCount(): Int = segments.size

    override fun getName(index: Int): T = createPath(false, listOf(getNameByteString(index)))

    fun getNameByteString(index: Int): ByteString = segments[index]

    override fun subpath(beginIndex: Int, endIndex: Int): T {
        val subSegments = segments.subList(beginIndex, endIndex).toList()
        return createPath(false, subSegments)
    }

    override fun startsWith(other: Path): Boolean {
        if (this === other) {
            return true
        }
        if (other.javaClass != javaClass) {
            return false
        }
        other as ByteStringListPath<*>
        return segments.startsWith(other.segments)
    }

    fun startsWith(other: ByteString): Boolean = startsWith(createPath(other))

    override fun endsWith(other: Path): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other.javaClass) {
            return false
        }
        other as ByteStringListPath<*>
        return segments.endsWith(other.segments)
    }

    fun endsWith(other: ByteString): Boolean = endsWith(createPath(other))

    override fun normalize(): T {
        val normalizedSegments = mutableListOf<ByteString>()
        for (segment in segments) {
            if (segment == BYTE_STRING_DOT) {
                // Ignored.
            } else if (segment == BYTE_STRING_DOT_DOT) {
                if (normalizedSegments.isEmpty()) {
                    if (!isAbsolute) {
                        normalizedSegments += segment
                    }
                } else {
                    if (normalizedSegments.last() == BYTE_STRING_DOT_DOT) {
                        normalizedSegments += segment
                    } else {
                        @OptIn(ExperimentalStdlibApi::class)
                        normalizedSegments.removeLast()
                    }
                }
            } else {
                normalizedSegments += segment
            }
        }
        if (!isAbsolute && normalizedSegments.isEmpty()) {
            return createEmptyPath()
        }
        return createPath(isAbsolute, normalizedSegments)
    }

    override fun resolve(other: Path): T {
        @Suppress("UNCHECKED_CAST")
        other as? T ?: throw ProviderMismatchException(other.toString())
        if (other.isAbsolute) {
            return other
        }
        if (other.isEmpty) {
            @Suppress("UNCHECKED_CAST")
            return this as T
        }
        if (isEmpty) {
            return other
        }
        val resolvedSegments = segments + other.segments
        return createPath(isAbsolute, resolvedSegments)
    }

    fun resolve(other: ByteString): T = resolve(createPath(other))

    fun resolveSibling(other: ByteString): T = resolveSibling(createPath(other))

    override fun relativize(other: Path): T {
        @Suppress("UNCHECKED_CAST")
        other as? T ?: throw ProviderMismatchException(other.toString())
        require(other.isAbsolute == isAbsolute) {
            "The other path must be as absolute as this path"
        }
        if (isEmpty) {
            return other
        }
        if (this == other) {
            return createEmptyPath()
        }
        val segmentsSize = segments.size
        val otherSegmentsSize = other.segments.size
        val minSegmentsSize = min(segmentsSize, otherSegmentsSize)
        var commonSegmentsSize = 0
        while (commonSegmentsSize < minSegmentsSize
            && segments[commonSegmentsSize] == other.segments[commonSegmentsSize]) {
            ++commonSegmentsSize
        }
        val relativeSegments = mutableListOf<ByteString>()
        val dotDotCount = segmentsSize - commonSegmentsSize
        if (dotDotCount > 0) {
            repeat(dotDotCount) { relativeSegments += BYTE_STRING_DOT_DOT }
        }
        if (commonSegmentsSize < otherSegmentsSize) {
            relativeSegments += other.segments.subList(commonSegmentsSize, otherSegmentsSize)
        }
        return createPath(false, relativeSegments)
    }

    override fun toUri(): URI = URI::class.create(uriScheme, uriAuthority, uriPath, uriQuery)

    override fun toAbsolutePath(): T {
        if (isAbsolute) {
            @Suppress("UNCHECKED_CAST")
            return this as T
        }
        return defaultDirectory.resolve(this)
    }

    open fun toByteString(): ByteString {
        // We are okay with the potential race condition here.
        var byteString = byteStringCache
        if (byteString == null) {
            val builder = ByteStringBuilder()
            if (isAbsolute && root != null) {
                builder.append(separator)
            }
            var first = true
            for (segment in segments) {
                if (first) {
                    first = false
                } else {
                    builder.append(separator)
                }
                builder.append(segment)
            }
            byteString = builder.toByteString()
            byteStringCache = byteString
        }
        return byteString
    }

    override fun toString(): String = toByteString().toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as ByteStringListPath<*>
        return separator == other.separator
            && segments == other.segments
            && isAbsolute == other.isAbsolute
            && fileSystem == other.fileSystem
    }

    override fun hashCode(): Int = hash(separator, segments, isAbsolute, fileSystem)

    override fun compareTo(other: Path): Int {
        other as? ByteStringListPath<*> ?: throw ProviderMismatchException(other.toString())
        return toByteString().compareTo(other.toByteString())
    }

    val nameByteStrings: Iterable<ByteString>
        get() = object : Iterable<ByteString> {
            override fun iterator(): Iterator<ByteString> = object : Iterator<ByteString> {
                private var index = 0

                override fun hasNext(): Boolean = index < nameCount

                override fun next(): ByteString {
                    if (index >= nameCount) {
                        throw NoSuchElementException()
                    }
                    val name = getNameByteString(index)
                    ++index
                    return name
                }
            }
        }

    val isEmpty: Boolean
        get() = !isAbsolute && segments.size == 1 && segments[0] == ByteString.EMPTY

    protected abstract fun isPathAbsolute(path: ByteString): Boolean

    protected abstract fun createPath(path: ByteString): T

    protected abstract fun createPath(absolute: Boolean, segments: List<ByteString>): T

    private fun createEmptyPath(): T = createPath(false, listOf(ByteString.EMPTY))

    protected open val uriScheme: String
        get() = fileSystem.provider().scheme

    protected open val uriAuthority: UriAuthority
        get() = UriAuthority.EMPTY

    protected open val uriPath: ByteString
        get() = toAbsolutePath().toByteString()

    protected open val uriQuery: ByteString?
        get() = null

    protected abstract val defaultDirectory: T

    protected constructor(source: Parcel) {
        separator = source.readByte()
        isAbsolute = source.readBooleanCompat()
        segments = source.readParcelableListCompat()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte(separator)
        dest.writeBooleanCompat(isAbsolute)
        dest.writeParcelableListCompat(segments, flags)
    }

    companion object {
        private val BYTE_STRING_DOT = ".".toByteString()
        private val BYTE_STRING_DOT_DOT = "..".toByteString()
    }
}
