package me.zhanghai.kotlin.filesystem

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.indices
import me.zhanghai.kotlin.filesystem.internal.UriParser
import me.zhanghai.kotlin.filesystem.internal.decodePart
import me.zhanghai.kotlin.filesystem.internal.encodeFragment
import me.zhanghai.kotlin.filesystem.internal.encodeHost
import me.zhanghai.kotlin.filesystem.internal.encodePath
import me.zhanghai.kotlin.filesystem.internal.encodeQuery
import me.zhanghai.kotlin.filesystem.internal.encodeUserInfo
import me.zhanghai.kotlin.filesystem.internal.parse
import me.zhanghai.kotlin.filesystem.internal.requireValidEncodedFragment
import me.zhanghai.kotlin.filesystem.internal.requireValidEncodedHost
import me.zhanghai.kotlin.filesystem.internal.requireValidEncodedPath
import me.zhanghai.kotlin.filesystem.internal.requireValidEncodedQuery
import me.zhanghai.kotlin.filesystem.internal.requireValidEncodedUserInfo
import me.zhanghai.kotlin.filesystem.internal.requireValidPort
import me.zhanghai.kotlin.filesystem.internal.requireValidScheme
import kotlin.experimental.and

// https://datatracker.ietf.org/doc/html/rfc3986
public class Uri
internal constructor(
    public val scheme: String?,
    public val encodedUserInfo: String?,
    public val encodedHost: String?,
    public val port: Int?,
    public val encodedPath: String,
    public val encodedQuery: String?,
    public val encodedFragment: String?,
    @Suppress("UNUSED_PARAMETER") any: Any?
) : Comparable<Uri> {
    public val encodedAuthority: String? by
        lazy(LazyThreadSafetyMode.NONE) {
            if (encodedUserInfo != null || encodedHost != null || port != null) {
                buildString {
                    if (encodedUserInfo != null) {
                        append(encodedUserInfo)
                        append('@')
                    }
                    if (encodedHost != null) {
                        append(encodedHost)
                    }
                    if (port != null) {
                        append(':')
                        append(port)
                    }
                }
            } else {
                null
            }
        }

    public val decodedAuthority: ByteString? by
        lazy(LazyThreadSafetyMode.NONE) { encodedAuthority?.let { UriParser.decodePart(it) } }

    public val decodedUserInfo: ByteString? by
        lazy(LazyThreadSafetyMode.NONE) { encodedUserInfo?.let { UriParser.decodePart(it) } }

    public val decodedHost: ByteString? by
        lazy(LazyThreadSafetyMode.NONE) { encodedHost?.let { UriParser.decodePart(it) } }

    public val decodedPath: ByteString by
        lazy(LazyThreadSafetyMode.NONE) { encodedPath.let { UriParser.decodePart(it) } }

    public val decodedQuery: ByteString? by
        lazy(LazyThreadSafetyMode.NONE) { encodedQuery?.let { UriParser.decodePart(it) } }

    public val decodedFragment: ByteString? by
        lazy(LazyThreadSafetyMode.NONE) { encodedFragment?.let { UriParser.decodePart(it) } }

    public fun copyEncoded(
        scheme: String? = this.scheme,
        encodedUserInfo: String? = this.encodedUserInfo,
        encodedHost: String? = this.encodedHost,
        port: Int? = this.port,
        encodedPath: String = this.encodedPath,
        encodedQuery: String? = this.encodedQuery,
        encodedFragment: String? = this.encodedFragment
    ): Uri {
        if (scheme !== this.scheme) {
            UriParser.requireValidScheme(scheme)
        }
        if (encodedUserInfo !== this.encodedUserInfo) {
            UriParser.requireValidEncodedUserInfo(encodedUserInfo)
        }
        if (encodedHost !== this.encodedHost) {
            UriParser.requireValidEncodedHost(encodedHost)
        }
        UriParser.requireValidPort(port)
        if (encodedPath != this.encodedPath) {
            UriParser.requireValidEncodedPath(encodedPath, scheme != null)
        }
        if (encodedQuery != this.encodedQuery) {
            UriParser.requireValidEncodedQuery(encodedQuery)
        }
        if (encodedFragment != this.encodedFragment) {
            UriParser.requireValidEncodedFragment(encodedFragment)
        }
        return Uri(
            scheme,
            encodedUserInfo,
            encodedHost,
            port,
            encodedPath,
            encodedQuery,
            encodedFragment,
            null
        )
    }

    public fun copyDecoded(
        scheme: String? = this.scheme,
        decodedUserInfo: ByteString? = BYTE_STRING_COPY,
        decodedHost: ByteString? = BYTE_STRING_COPY,
        port: Int? = this.port,
        decodedPath: ByteString = BYTE_STRING_COPY,
        decodedQuery: ByteString? = BYTE_STRING_COPY,
        decodedFragment: ByteString? = BYTE_STRING_COPY
    ): Uri {
        if (scheme !== this.scheme) {
            UriParser.requireValidScheme(scheme)
        }
        val encodedUserInfo =
            if (decodedUserInfo === BYTE_STRING_COPY) {
                encodedUserInfo
            } else {
                decodedUserInfo?.let { UriParser.encodeUserInfo(it) }
            }
        val encodedHost =
            if (decodedHost === BYTE_STRING_COPY) {
                encodedHost
            } else {
                decodedHost?.let { UriParser.encodeHost(it) }
            }
        UriParser.requireValidPort(port)
        val encodedPath =
            if (decodedPath === BYTE_STRING_COPY) {
                encodedPath
            } else {
                decodedPath
                    .let { UriParser.encodePath(it) }
                    .also { UriParser.requireValidEncodedPath(it, scheme != null) }
            }
        val encodedQuery =
            if (decodedQuery === BYTE_STRING_COPY) {
                encodedQuery
            } else {
                decodedQuery?.let { UriParser.encodeQuery(it) }
            }
        val encodedFragment =
            if (decodedFragment === BYTE_STRING_COPY) {
                encodedFragment
            } else {
                decodedFragment?.let { UriParser.encodeFragment(it) }
            }
        return Uri(
            scheme,
            encodedUserInfo,
            encodedHost,
            port,
            encodedPath,
            encodedQuery,
            encodedFragment,
            null
        )
    }

    private val string: String by
        lazy(LazyThreadSafetyMode.NONE) {
            buildString {
                if (scheme != null) {
                    append(scheme)
                    append(':')
                }
                if (encodedHost != null) {
                    append("//")
                    if (encodedUserInfo != null) {
                        append(encodedUserInfo)
                        append('@')
                    }
                    append(encodedHost)
                    if (port != null) {
                        append(':')
                        append(port)
                    }
                }
                append(encodedPath)
                if (encodedQuery != null) {
                    append('?')
                    append(encodedQuery)
                }
                if (encodedFragment != null) {
                    append('#')
                    append(encodedFragment)
                }
            }
        }

    override fun compareTo(other: Uri): Int = string.compareTo(other.string)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }
        other as Uri
        return string == other.string
    }

    override fun hashCode(): Int = string.hashCode()

    override fun toString(): String = string

    public companion object {
        public fun ofEncoded(
            scheme: String? = null,
            encodedUserInfo: String? = null,
            encodedHost: String? = null,
            port: Int? = null,
            encodedPath: String = "",
            encodedQuery: String? = null,
            encodedFragment: String? = null
        ): Uri {
            UriParser.requireValidScheme(scheme)
            UriParser.requireValidEncodedUserInfo(encodedUserInfo)
            UriParser.requireValidEncodedHost(encodedHost)
            UriParser.requireValidPort(port)
            UriParser.requireValidEncodedPath(encodedPath, scheme != null)
            UriParser.requireValidEncodedQuery(encodedQuery)
            UriParser.requireValidEncodedFragment(encodedFragment)
            return Uri(
                scheme,
                encodedUserInfo,
                encodedHost,
                port,
                encodedPath,
                encodedQuery,
                encodedFragment,
                null
            )
        }

        public fun ofDecoded(
            scheme: String? = null,
            decodedUserInfo: ByteString? = null,
            decodedHost: ByteString? = null,
            port: Int? = null,
            decodedPath: ByteString = BYTE_STRING_EMPTY,
            decodedQuery: ByteString? = null,
            decodedFragment: ByteString? = null
        ): Uri {
            UriParser.requireValidScheme(scheme)
            val encodedUserInfo = decodedUserInfo?.let { UriParser.encodeUserInfo(it) }
            val encodedHost = decodedHost?.let { UriParser.encodeHost(it) }
            UriParser.requireValidPort(port)
            val encodedPath =
                decodedPath
                    .let { UriParser.encodePath(it) }
                    .also { UriParser.requireValidEncodedPath(it, scheme != null) }
            val encodedQuery = decodedQuery?.let { UriParser.encodeQuery(it) }
            val encodedFragment = decodedFragment?.let { UriParser.encodeFragment(it) }
            return Uri(
                scheme,
                encodedUserInfo,
                encodedHost,
                port,
                encodedPath,
                encodedQuery,
                encodedFragment,
                null
            )
        }

        public fun parse(uri: String): Uri = UriParser.parse(uri)
    }
}

private val BYTE_STRING_COPY = ByteString(0.toByte())
private val BYTE_STRING_EMPTY = ByteString()
