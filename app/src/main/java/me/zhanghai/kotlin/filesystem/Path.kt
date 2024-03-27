package me.zhanghai.kotlin.filesystem

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.append
import kotlinx.io.bytestring.buildByteString
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.io.bytestring.isNotEmpty
import me.zhanghai.kotlin.filesystem.internal.compareTo
import me.zhanghai.kotlin.filesystem.internal.contains
import me.zhanghai.kotlin.filesystem.internal.endsWith
import me.zhanghai.kotlin.filesystem.internal.startsWith
import kotlin.math.min

public class Path
private constructor(
    public val rootUri: Uri,
    public val isAbsolute: Boolean,
    public val names: List<ByteString>
) : Comparable<Path>, Iterable<ByteString> by names {
    init {
        requireNotNull(rootUri.scheme) { "Invalid root URI in path" }
        require(rootUri.isAbsolute && rootUri.decodedPath == NAME_SEPARATOR) {
            "Invalid root URI in path"
        }
        names.forEach {
            require(it.isNotEmpty()) { "Empty name in path" }
            require(NAME_SEPARATOR_BYTE !in it) { "Invalid name in path" }
        }
    }

    public val scheme: String
        get() = rootUri.scheme!!

    public val fileName: ByteString?
        get() = names.lastOrNull()

    public fun getParent(): Path? {
        val lastIndex = names.lastIndex
        return if (lastIndex >= 0) {
            Path(rootUri, isAbsolute, names.subList(0, lastIndex))
        } else {
            null
        }
    }

    public fun subPath(startIndex: Int, endIndex: Int): Path =
        if (startIndex == 0 && endIndex == names.size) {
            this
        } else {
            Path(rootUri, isAbsolute, names.subList(startIndex, endIndex))
        }

    public fun startsWith(other: Path): Boolean {
        if (this === other) {
            return true
        }
        if (rootUri != other.rootUri) {
            return false
        }
        return isAbsolute == other.isAbsolute && names.startsWith(other.names)
    }

    public fun endsWith(other: Path): Boolean {
        if (this === other) {
            return true
        }
        if (rootUri != other.rootUri) {
            return false
        }
        return if (other.isAbsolute) {
            isAbsolute && names == other.names
        } else {
            names.endsWith(other.names)
        }
    }

    public fun normalize(): Path {
        var newNames: MutableList<ByteString>? = null
        for ((index, name) in names.withIndex()) {
            when (name) {
                NAME_DOT ->
                    if (newNames == null) {
                        newNames = names.subList(0, index).toMutableList()
                    }
                NAME_DOT_DOT ->
                    if (newNames != null) {
                        when (newNames.lastOrNull()) {
                            null ->
                                if (!isAbsolute) {
                                    newNames += name
                                }
                            NAME_DOT_DOT -> newNames += name
                            else -> newNames.removeLast()
                        }
                    } else {
                        when (names.getOrNull(index - 1)) {
                            null ->
                                if (isAbsolute) {
                                    newNames = mutableListOf()
                                }
                            NAME_DOT_DOT -> {}
                            else -> newNames = names.subList(0, index - 1).toMutableList()
                        }
                    }
                else ->
                    if (newNames != null) {
                        newNames += name
                    }
            }
        }
        if (newNames == null) {
            return this
        }
        return Path(rootUri, isAbsolute, newNames)
    }

    public fun resolve(fileName: ByteString): Path = Path(rootUri, isAbsolute, names + fileName)

    public fun resolve(other: Path): Path {
        require(rootUri == other.rootUri) { "Cannot resolve a path with a different root URI" }
        return if (other.isAbsolute) {
            other
        } else {
            Path(rootUri, isAbsolute, names + other.names)
        }
    }

    public fun resolveSibling(fileName: ByteString): Path {
        check(names.isNotEmpty()) { "Cannot resolve sibling of an empty path" }
        return Path(rootUri, isAbsolute, names.toMutableList().apply { set(lastIndex, fileName) })
    }

    public fun relativize(other: Path): Path {
        if (this === other) {
            return Path(rootUri, false, emptyList())
        }
        require(rootUri == other.rootUri) { "Cannot relativize a path with a different root URI" }
        require(isAbsolute == other.isAbsolute) {
            "Cannot relativize a path with a different absoluteness"
        }
        if (names.isEmpty()) {
            return if (other.isAbsolute) {
                Path(rootUri, false, other.names)
            } else {
                other
            }
        }
        val namesSize = names.size
        val otherNamesSize = other.names.size
        val minNamesSize = min(namesSize, otherNamesSize)
        var commonNamesSize = 0
        while (commonNamesSize < minNamesSize) {
            if (names[commonNamesSize] != other.names[commonNamesSize]) {
                break
            }
            ++commonNamesSize
        }
        val newNames = names.subList(0, commonNamesSize).toMutableList()
        repeat(namesSize - commonNamesSize) { newNames += NAME_DOT_DOT }
        newNames += other.names.subList(commonNamesSize, otherNamesSize)
        return Path(rootUri, false, newNames)
    }

    public fun toUri(): Uri {
        check(isAbsolute) { "Cannot convert a relative path to URI" }
        val decodedPath = buildByteString {
            for (name in names) {
                append(NAME_SEPARATOR_BYTE)
                append(name)
            }
        }
        return rootUri.copyDecoded(decodedPath = decodedPath)
    }

    override fun compareTo(other: Path): Int {
        rootUri.compareTo(other.rootUri).let {
            if (it != 0) {
                return it
            }
        }
        isAbsolute.compareTo(other.isAbsolute).let {
            if (it != 0) {
                return it
            }
        }
        return names.compareTo(other.names)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }
        other as Path
        return rootUri == other.rootUri && isAbsolute == other.isAbsolute && names == other.names
    }

    override fun hashCode(): Int {
        var result = rootUri.hashCode()
        result = 31 * result + isAbsolute.hashCode()
        result = 31 * result + names.hashCode()
        return result
    }

    override fun toString(): String = toUri().toString()

    public companion object {
        private val NAME_DOT = ".".encodeToByteString()
        private val NAME_DOT_DOT = "..".encodeToByteString()
        private val NAME_SEPARATOR = "/".encodeToByteString()
        private const val NAME_SEPARATOR_BYTE = '/'.code.toByte()
    }
}
