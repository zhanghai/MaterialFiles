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
    public val names: List<ByteString>,
    @Suppress("UNUSED_PARAMETER") any: Any?
) : Comparable<Path> {
    public val scheme: String
        get() = rootUri.scheme!!

    public val fileName: ByteString?
        get() = names.lastOrNull()

    public fun getParent(): Path? {
        val lastIndex = names.lastIndex
        return if (lastIndex >= 0) {
            Path(rootUri, isAbsolute, names.subList(0, lastIndex), null)
        } else {
            null
        }
    }

    public fun subPath(startIndex: Int, endIndex: Int): Path =
        if (startIndex == 0 && endIndex == names.size) {
            this
        } else {
            Path(rootUri, isAbsolute, names.subList(startIndex, endIndex), null)
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
        return Path(rootUri, isAbsolute, newNames, null)
    }

    public fun resolve(fileName: ByteString): Path =
        Path(rootUri, isAbsolute, names + fileName, null)

    public fun resolve(other: Path): Path {
        require(rootUri == other.rootUri) { "Cannot resolve a path with a different root URI" }
        return if (other.isAbsolute) {
            other
        } else {
            Path(rootUri, isAbsolute, names + other.names, null)
        }
    }

    public fun resolveSibling(fileName: ByteString): Path {
        check(names.isNotEmpty()) { "Cannot resolve sibling of an empty path" }
        return Path(
            rootUri,
            isAbsolute,
            names.toMutableList().apply { set(lastIndex, fileName) },
            null
        )
    }

    public fun relativize(other: Path): Path {
        if (this === other) {
            return Path(rootUri, false, emptyList(), null)
        }
        require(rootUri == other.rootUri) { "Cannot relativize a path with a different root URI" }
        require(isAbsolute == other.isAbsolute) {
            "Cannot relativize a path with a different absoluteness"
        }
        if (names.isEmpty()) {
            return if (other.isAbsolute) {
                Path(rootUri, false, other.names, null)
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
        return Path(rootUri, false, newNames, null)
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

    override fun toString(): String = "Path(rootUri=$rootUri, isAbsolute=$isAbsolute, names=$names)"

    public companion object {
        private val NAME_DOT = ".".encodeToByteString()
        private val NAME_DOT_DOT = "..".encodeToByteString()
        private const val NAME_SEPARATOR_CHAR = '/'
        private const val NAME_SEPARATOR_BYTE = NAME_SEPARATOR_CHAR.code.toByte()
        private const val NAME_SEPARATOR_STRING = "/"

        public fun of(rootUri: Uri, isAbsolute: Boolean, names: List<ByteString>): Path {
            requireNotNull(rootUri.scheme) { "Missing scheme in path root URI \"$rootUri\"" }
            require(rootUri.encodedPath == NAME_SEPARATOR_STRING) {
                "Path is not root in path root URI \"$rootUri\""
            }
            for (name in names) {
                require(name.isNotEmpty()) { "Empty name in path name \"$name\$" }
                require(NAME_SEPARATOR_BYTE !in name) { "Name separator in path name \"$name\"" }
            }
            return Path(rootUri, isAbsolute, names, null)
        }

        public fun fromUri(uri: Uri): Path {
            requireNotNull(uri.scheme) { "Missing scheme in path URI \"$uri\"" }
            val encodedPath = uri.encodedPath
            require(encodedPath.isNotEmpty()) { "Empty path in path URI \"$uri\"" }
            require(encodedPath[0] == NAME_SEPARATOR_CHAR) { "Relative path in path URI \"$uri\"" }
            val rootUri = uri.copyEncoded(encodedPath = NAME_SEPARATOR_STRING)
            val names = buildList {
                val decodedPath = uri.decodedPath
                val decodedPathSize = decodedPath.size
                var nameStart = 0
                var nameEnd = nameStart
                while (nameEnd < decodedPathSize) {
                    if (decodedPath[nameEnd] == NAME_SEPARATOR_BYTE) {
                        if (nameEnd != nameStart) {
                            this += decodedPath.substring(nameStart, nameEnd)
                        }
                        nameStart = nameEnd + 1
                        nameEnd = nameStart
                    } else {
                        ++nameEnd
                    }
                }
                if (nameEnd != nameStart) {
                    this += decodedPath.substring(nameStart, nameEnd)
                }
            }
            return Path(rootUri, true, names, null)
        }
    }
}
