package me.zhanghai.kotlin.filesystem.internal

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.buildByteString
import kotlinx.io.bytestring.decodeToString
import me.zhanghai.kotlin.filesystem.Uri
import kotlin.experimental.and
import kotlin.experimental.or

internal fun UriParser.parse(input: String): Uri {
    val listener = UriParserListener()
    parseUriReference(input, listener = listener).let {
        require(it == input.length) { "Invalid URI \"$input\"" }
    }
    return listener.createUri()
}

private class UriParserListener : UriParser.Listener {
    private var scheme: String? = null
    private var encodedUserInfo: String? = null
    private var encodedHost: String? = null
    private var port: Int? = null
    private var encodedPath: String? = null
    private var encodedQuery: String? = null
    private var encodedFragment: String? = null

    override fun exitScheme(input: String, startIndex: Int, endIndex: Int) {
        if (endIndex != -1) {
            scheme = input.substring(startIndex, endIndex)
        }
    }

    override fun exitUserinfo(input: String, startIndex: Int, endIndex: Int) {
        if (endIndex != -1) {
            encodedUserInfo = input.substring(startIndex, endIndex)
        }
    }

    override fun exitHost(input: String, startIndex: Int, endIndex: Int) {
        if (endIndex != -1) {
            encodedHost = input.substring(startIndex, endIndex)
        }
    }

    override fun exitPort(input: String, startIndex: Int, endIndex: Int) {
        if (endIndex != -1) {
            port = input.substring(startIndex, endIndex).toInt()
        }
    }

    override fun exitPathAbempty(input: String, startIndex: Int, endIndex: Int) {
        exitPathCommon(input, startIndex, endIndex)
    }

    override fun exitPathAbsolute(input: String, startIndex: Int, endIndex: Int) {
        exitPathCommon(input, startIndex, endIndex)
    }

    override fun exitPathNoscheme(input: String, startIndex: Int, endIndex: Int) {
        exitPathCommon(input, startIndex, endIndex)
    }

    override fun exitPathRootless(input: String, startIndex: Int, endIndex: Int) {
        exitPathCommon(input, startIndex, endIndex)
    }

    override fun exitPathEmpty(input: String, startIndex: Int, endIndex: Int) {
        exitPathCommon(input, startIndex, endIndex)
    }

    private fun exitPathCommon(input: String, startIndex: Int, endIndex: Int) {
        if (endIndex != -1) {
            encodedPath = input.substring(startIndex, endIndex)
        }
    }

    override fun exitQuery(input: String, startIndex: Int, endIndex: Int) {
        if (endIndex != -1) {
            encodedQuery = input.substring(startIndex, endIndex)
        }
    }

    override fun exitFragment(input: String, startIndex: Int, endIndex: Int) {
        if (endIndex != -1) {
            encodedFragment = input.substring(startIndex, endIndex)
        }
    }

    fun createUri(): Uri =
        Uri(
            scheme,
            encodedUserInfo,
            encodedHost,
            port,
            encodedPath!!,
            encodedQuery,
            encodedFragment,
            null
        )
}

internal fun UriParser.requireValidScheme(scheme: String?) {
    if (scheme != null) {
        require(parseScheme(scheme) == scheme.length) { "Invalid URI scheme \"$scheme\"" }
    }
}

internal fun UriParser.requireValidEncodedUserInfo(encodedUserInfo: String?) {
    if (encodedUserInfo != null) {
        require(parseUserinfo(encodedUserInfo) == encodedUserInfo.length) {
            "Invalid URI user info \"$encodedUserInfo\""
        }
    }
}

internal fun UriParser.requireValidEncodedHost(encodedHost: String?) {
    if (encodedHost != null) {
        require(parseHost(encodedHost) == encodedHost.length) {
            "Invalid URI host \"$encodedHost\""
        }
    }
}

@Suppress("UnusedReceiverParameter")
internal fun UriParser.requireValidPort(port: Int?) {
    if (port != null) {
        require(port >= 0) { "Invalid URI port $port" }
    }
}

internal fun UriParser.requireValidEncodedPath(encodedPath: String, hasScheme: Boolean) {
    if (parsePathAbempty(encodedPath) == encodedPath.length) {
        return
    }
    if (parsePathAbsolute(encodedPath) == encodedPath.length) {
        return
    }
    if (hasScheme) {
        if (parsePathRootless(encodedPath) == encodedPath.length) {
            return
        }
    } else {
        if (parsePathNoscheme(encodedPath) == encodedPath.length) {
            return
        }
    }
    if (parsePathEmpty(encodedPath) == encodedPath.length) {
        return
    }
    throw IllegalArgumentException("Invalid URI path \"$encodedPath\"")
}

internal fun UriParser.requireValidEncodedQuery(encodedQuery: String?) {
    if (encodedQuery != null) {
        require(parseQuery(encodedQuery) == encodedQuery.length) {
            "Invalid URI query \"$encodedQuery\""
        }
    }
}

internal fun UriParser.requireValidEncodedFragment(encodedFragment: String?) {
    if (encodedFragment != null) {
        require(parseFragment(encodedFragment) == encodedFragment.length) {
            "Invalid URI fragment \"$encodedFragment\""
        }
    }
}

@Suppress("UnusedReceiverParameter")
internal fun UriParser.encodeUserInfo(decodedUserInfo: ByteString): String =
    encodePart(decodedUserInfo, CHAR_SET_USERINFO)

internal fun UriParser.encodeHost(decodedHost: ByteString): String {
    if (
        decodedHost.size > 2 &&
            decodedHost.first() == '['.code.toByte() &&
            decodedHost.last() == ']'.code.toByte()
    ) {
        val encodedHost = decodedHost.decodeToString()
        if (parseIpLiteral(encodedHost) == encodedHost.length) {
            return encodedHost
        }
    }
    return encodePart(decodedHost, CHAR_SET_REG_NAME)
}

@Suppress("UnusedReceiverParameter")
internal fun UriParser.encodePath(decodedPath: ByteString): String =
    encodePart(decodedPath, CHAR_SET_PATH)

@Suppress("UnusedReceiverParameter")
internal fun UriParser.encodeQuery(decodedQuery: ByteString): String =
    encodePart(decodedQuery, CHAR_SET_QUERY)

@Suppress("UnusedReceiverParameter")
internal fun UriParser.encodeFragment(decodedFragment: ByteString): String =
    encodePart(decodedFragment, CHAR_SET_FRAGMENT)

// https://datatracker.ietf.org/doc/html/rfc3986#appendix-A
private val CHAR_SET_ALPHA = AsciiCharSet.ofRange('A', 'Z') or AsciiCharSet.ofRange('a', 'z')
private val CHAR_SET_DIGIT = AsciiCharSet.ofRange('0', '9')
private val CHAR_SET_SUB_DELIMS = AsciiCharSet.of("!$&'()*+,;=")
private val CHAR_SET_UNRESERVED = CHAR_SET_ALPHA or CHAR_SET_DIGIT or AsciiCharSet.of("-._~")
private val CHAR_SET_USERINFO = CHAR_SET_UNRESERVED or CHAR_SET_SUB_DELIMS or AsciiCharSet.of(':')
private val CHAR_SET_REG_NAME = CHAR_SET_UNRESERVED or CHAR_SET_SUB_DELIMS
private val CHAR_SET_PCHAR = CHAR_SET_UNRESERVED or CHAR_SET_SUB_DELIMS or AsciiCharSet.of(":@")
private val CHAR_SET_PATH = CHAR_SET_PCHAR or AsciiCharSet.of('/')
private val CHAR_SET_QUERY = CHAR_SET_PCHAR or AsciiCharSet.of("/?")
private val CHAR_SET_FRAGMENT = CHAR_SET_PCHAR or AsciiCharSet.of("/?")

private fun encodePart(
    decodedPart: ByteString,
    charSet: AsciiCharSet,
    startIndex: Int = 0,
    endIndex: Int = decodedPart.size
): String = buildString {
    for (i in startIndex ..< endIndex) {
        val byte = decodedPart[i]
        val char = byte.toInt().toChar()
        if (charSet.matches(char)) {
            append(char)
        } else {
            append('%')
            append((((byte.toInt() ushr 4).toByte() and 0x0F)).hexEncode())
            append((byte and 0x0F).hexEncode())
        }
    }
}

private fun Byte.hexEncode(): Char =
    when (this) {
        in 0..9 -> '0' + toInt()
        in 10..15 -> 'A' + (toInt() - 10)
        else -> throw IllegalArgumentException("Non-half byte $this in URI percent-encoding")
    }

@Suppress("UnusedReceiverParameter")
internal fun UriParser.decodePart(encodedPart: String): ByteString = buildByteString {
    var index = 0
    val length = encodedPart.length
    while (index < length) {
        when (val char = encodedPart[index]) {
            '%' -> {
                require(index + 3 <= length) {
                    "Incomplete URI percent-encoding \"${encodedPart.substring(index)}\""
                }
                val halfByte1 = encodedPart[index + 1].hexDecode()
                val halfByte2 = encodedPart[index + 2].hexDecode()
                val byte = (halfByte1.toInt() shl 4).toByte() or halfByte2
                append(byte)
                index += 3
            }
            else -> {
                append(char.code.toByte())
                ++index
            }
        }
    }
}

private fun Char.hexDecode(): Byte =
    when (this) {
        in '0'..'9' -> (this - '0').toByte()
        in 'A'..'F' -> (10 + (this - 'A')).toByte()
        in 'a'..'f' -> (10 + (this - 'a')).toByte()
        else -> throw IllegalArgumentException("Invalid character '$this' in URI percent-encoding")
    }
