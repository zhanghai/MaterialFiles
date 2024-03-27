package me.zhanghai.kotlin.filesystem.internal

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.buildByteString
import me.zhanghai.kotlin.filesystem.Uri
import kotlin.experimental.and
import kotlin.experimental.or

internal object UriParser {
    // https://datatracker.ietf.org/doc/html/rfc3986#appendix-A
    private val CHAR_MASK_ALPHA = CharMask.ofRange('A', 'Z') or CharMask.ofRange('a', 'z')
    private val CHAR_MASK_DIGIT = CharMask.ofRange('0', '9')
    private val CHAR_MASK_HEX_DIGIT =
        CHAR_MASK_DIGIT or CharMask.ofRange('A', 'F') or CharMask.ofRange('a', 'f')
    private val CHAR_MASK_SUB_DELIMS = CharMask.of("!$&'()*+,;=")
    private val CHAR_MASK_UNRESERVED = CHAR_MASK_ALPHA or CHAR_MASK_DIGIT or CharMask.of("-._~")
    private val CHAR_MASK_SCHEME_FIRST = CHAR_MASK_ALPHA
    private val CHAR_MASK_SCHEME_REST = CHAR_MASK_ALPHA or CHAR_MASK_DIGIT or CharMask.of("+-.")
    private val CHAR_MASK_USERINFO =
        CHAR_MASK_UNRESERVED or CHAR_MASK_SUB_DELIMS or CharMask.of(':')
    private val CHAR_MASK_REG_NAME = CHAR_MASK_UNRESERVED or CHAR_MASK_SUB_DELIMS
    private val CHAR_MASK_IP_LITERAL_ADDRESS =
        CHAR_MASK_ALPHA or CHAR_MASK_DIGIT or CharMask.of(".:")
    private val CHAR_MASK_PCHAR = CHAR_MASK_UNRESERVED or CHAR_MASK_SUB_DELIMS or CharMask.of(":@")
    private val CHAR_MASK_PCHAR_NC = CHAR_MASK_PCHAR and CharMask.of(':').inv()
    private val CHAR_MASK_PATH = CHAR_MASK_PCHAR or CharMask.of('/')
    private val CHAR_MASK_QUERY = CHAR_MASK_PCHAR or CharMask.of("/?")
    private val CHAR_MASK_FRAGMENT = CHAR_MASK_PCHAR or CharMask.of("/?")

    // https://datatracker.ietf.org/doc/html/rfc3986#appendix-B
    fun parse(input: String): Uri {
        var endIndex = 0
        val scheme =
            parseSuffixPartGroup(input, endIndex, UriParser::parseScheme, ':').let {
                if (it == endIndex) {
                    return@let null
                }
                input.substring(endIndex, it - 1).also { _ -> endIndex = it }
            }
        val encodedUserInfo: String?
        val encodedHost: String?
        val port: Int?
        run {
            var pendingEndIndex = endIndex
            parseCharSequence(input, pendingEndIndex, "//").let {
                if (it == pendingEndIndex) {
                    encodedUserInfo = null
                    encodedHost = null
                    port = null
                    return@run
                }
                pendingEndIndex = it
            }
            encodedUserInfo =
                parseSuffixPartGroup(input, pendingEndIndex, UriParser::parseUserInfo, '@').let {
                    if (it == pendingEndIndex) {
                        return@let null
                    }
                    input.substring(pendingEndIndex, it - 1).also { _ -> pendingEndIndex = it }
                }
            encodedHost =
                parseHost(input, pendingEndIndex).let {
                    if (it == pendingEndIndex) {
                        return@let ""
                    }
                    input.substring(pendingEndIndex, it).also { _ -> pendingEndIndex = it }
                }
            port =
                parsePortGroup(input, pendingEndIndex).let { (it, port) ->
                    if (it == pendingEndIndex) {
                        return@let null
                    }
                    pendingEndIndex = it
                    port
                }
            endIndex = pendingEndIndex
        }
        val encodedPath =
            parsePath(input, endIndex, scheme == null).let {
                if (it == endIndex) {
                    return@let ""
                } else {
                    input.substring(endIndex, it).also { _ -> endIndex = it }
                }
            }
        val encodedQuery =
            parsePrefixPartGroup(input, endIndex, '?', UriParser::parseQuery).let {
                if (it == endIndex) {
                    return@let null
                } else {
                    input.substring(endIndex, it).also { _ -> endIndex = it }
                }
            }
        val encodedFragment =
            parsePrefixPartGroup(input, endIndex, '#', UriParser::parseFragment).let {
                if (it == endIndex) {
                    return@let null
                } else {
                    input.substring(endIndex, it).also { _ -> endIndex = it }
                }
            }
        require(endIndex == input.length) { "Cannot parse URI \"$input\" at index $endIndex" }
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

    private inline fun parsePrefixPartGroup(
        input: String,
        startIndex: Int,
        prefixChar: Char,
        parsePart: (String, Int) -> Int
    ): Int =
        parseSequence(
            input,
            startIndex,
            @Suppress("NAME_SHADOWING") { input, startIndex ->
                parseChar(input, startIndex, prefixChar)
            },
            parsePart
        )

    private inline fun parseSuffixPartGroup(
        input: String,
        startIndex: Int,
        parsePart: (String, Int) -> Int,
        suffixChar: Char
    ): Int =
        @Suppress("MoveLambdaOutsideParentheses")
        (parseSequence(
        input,
        startIndex,
        parsePart,
        @Suppress("NAME_SHADOWING") { input, startIndex ->
            parseChar(input, startIndex, suffixChar)
        }
    ))

    private fun parsePortGroup(input: String, startIndex: Int): IntPair {
        var pendingEndIndex = startIndex
        parseChar(input, pendingEndIndex, ':').let {
            if (it == pendingEndIndex) {
                return IntPair(startIndex, -1)
            }
            pendingEndIndex = it
        }
        val port =
            parsePort(input, pendingEndIndex).let { (it, port) ->
                if (it == pendingEndIndex) {
                    return IntPair(startIndex, -1)
                }
                pendingEndIndex = it
                port
            }
        return IntPair(pendingEndIndex, port)
    }

    fun requireValidScheme(scheme: String?) {
        if (scheme != null) {
            require(parseScheme(scheme, 0) == scheme.length) { "Invalid URI scheme \"$scheme\"" }
        }
    }

    fun requireValidEncodedUserInfo(encodedUserInfo: String?) {
        if (encodedUserInfo != null) {
            require(parseUserInfo(encodedUserInfo, 0) == encodedUserInfo.length) {
                "Invalid URI user info \"$encodedUserInfo\""
            }
        }
    }

    fun requireValidEncodedHost(encodedHost: String?) {
        if (encodedHost != null) {
            require(parseUserInfo(encodedHost, 0) == encodedHost.length) {
                "Invalid URI host \"$encodedHost\""
            }
        }
    }

    fun requireValidPort(port: Int?) {
        if (port != null) {
            require(port >= 0) { "Invalid URI port $port" }
        }
    }

    fun requireValidEncodedPath(encodedPath: String, noScheme: Boolean) {
        require(parsePath(encodedPath, 0, noScheme) == encodedPath.length) {
            "Invalid URI path \"$encodedPath\""
        }
    }

    fun requireValidEncodedQuery(encodedQuery: String?) {
        if (encodedQuery != null) {
            require(parseQuery(encodedQuery, 0) == encodedQuery.length) {
                "Invalid URI query \"$encodedQuery\""
            }
        }
    }

    fun requireValidEncodedFragment(encodedFragment: String?) {
        if (encodedFragment != null) {
            require(parseUserInfo(encodedFragment, 0) == encodedFragment.length) {
                "Invalid URI fragment \"$encodedFragment\""
            }
        }
    }

    private fun parseScheme(input: String, startIndex: Int): Int {
        var endIndex = startIndex
        parseChar(input, endIndex, CHAR_MASK_SCHEME_FIRST).let {
            if (it == endIndex) {
                return endIndex
            }
            endIndex = it
        }
        return parseCharSequence(input, endIndex, CHAR_MASK_SCHEME_REST, false)
    }

    private fun parseUserInfo(input: String, startIndex: Int): Int =
        parseCharSequence(input, startIndex, CHAR_MASK_USERINFO, true)

    private fun parseHost(input: String, startIndex: Int): Int {
        parseIpLiteral(input, startIndex).let {
            if (it != startIndex) {
                return it
            }
        }
        return parseRegName(input, startIndex)
    }

    private fun parseIpLiteral(input: String, startIndex: Int): Int {
        var pendingEndIndex = startIndex
        parseChar(input, pendingEndIndex, '[').let {
            if (it == pendingEndIndex) {
                return startIndex
            }
            pendingEndIndex = it
        }
        parseIpLiteralAddress(input, pendingEndIndex).let {
            if (it == pendingEndIndex) {
                return startIndex
            }
            pendingEndIndex = it
        }
        parseChar(input, pendingEndIndex, ']').let {
            if (it == pendingEndIndex) {
                return startIndex
            }
            pendingEndIndex = it
        }
        return pendingEndIndex
    }

    // FIXME: Properly parse IPvFuture or IPv6 addresses.
    private fun parseIpLiteralAddress(input: String, startIndex: Int): Int =
        // Allow IPv6 scope ID with percent-encoded %.
        parseCharSequence(input, startIndex, CHAR_MASK_IP_LITERAL_ADDRESS, true)

    private fun parseRegName(input: String, startIndex: Int): Int =
        parseCharSequence(input, startIndex, CHAR_MASK_REG_NAME, true)

    private fun parsePort(input: String, startIndex: Int): IntPair {
        var endIndex = startIndex
        val inputLength = input.length
        var port = 0
        while (endIndex < inputLength) {
            val char = input[endIndex]
            if (CHAR_MASK_DIGIT.matches(char)) {
                port = port * 10 + (char.code - '0'.code)
                ++endIndex
                continue
            }
            break
        }
        return IntPair(endIndex, port)
    }

    private fun parsePath(input: String, startIndex: Int, noScheme: Boolean): Int {
        var endIndex = startIndex
        if (noScheme) {
            parseCharSequence(input, endIndex, CHAR_MASK_PCHAR_NC, true).let {
                if (it == endIndex) {
                    return endIndex
                }
                endIndex = it
            }
            parseChar(input, endIndex, '/').let {
                if (it == endIndex) {
                    return endIndex
                }
                endIndex = it
            }
        }
        return parseCharSequence(input, endIndex, CHAR_MASK_PATH, true)
    }

    private fun parseQuery(input: String, startIndex: Int): Int =
        parseCharSequence(input, startIndex, CHAR_MASK_QUERY, true)

    private fun parseFragment(input: String, startIndex: Int): Int =
        parseCharSequence(input, startIndex, CHAR_MASK_FRAGMENT, true)

    private fun parseChar(input: String, startIndex: Int, char: Char): Int {
        if (startIndex >= input.length) {
            return startIndex
        }
        if (input[startIndex] != char) {
            return startIndex
        }
        return startIndex + 1
    }

    private fun parseChar(input: String, startIndex: Int, charMask: CharMask): Int {
        if (startIndex >= input.length) {
            return startIndex
        }
        if (!charMask.matches(input[startIndex])) {
            return startIndex
        }
        return startIndex + 1
    }

    private fun parseCharSequence(input: String, startIndex: Int, string: String): Int {
        if (!input.startsWith(string, startIndex)) {
            return startIndex
        }
        return startIndex + string.length
    }

    private fun parseCharSequence(
        input: String,
        startIndex: Int,
        charMask: CharMask,
        allowPercentEncoded: Boolean
    ): Int {
        var endIndex = startIndex
        val inputLength = input.length
        while (endIndex < inputLength) {
            if (charMask.matches(input[endIndex])) {
                ++endIndex
                continue
            }
            if (allowPercentEncoded) {
                parsePercentEncoded(input, endIndex).let {
                    if (it == endIndex) {
                        return@let false
                    }
                    endIndex = it
                    true
                } && continue
            }
            break
        }
        return endIndex
    }

    private fun parsePercentEncoded(input: String, startIndex: Int): Int {
        if (startIndex + 2 >= input.length) {
            return startIndex
        }
        if (input[startIndex] != '%') {
            return startIndex
        }
        if (CHAR_MASK_HEX_DIGIT.matches(input[startIndex + 1])) {
            return startIndex
        }
        if (CHAR_MASK_HEX_DIGIT.matches(input[startIndex + 2])) {
            return startIndex
        }
        return startIndex + 3
    }

    private inline fun parseSequence(
        input: String,
        startIndex: Int,
        parse1: (String, Int) -> Int,
        parse2: (String, Int) -> Int
    ): Int {
        var pendingEndIndex = startIndex
        parse1(input, pendingEndIndex).let {
            if (it == pendingEndIndex) {
                return startIndex
            }
            pendingEndIndex = it
        }
        parse2(input, pendingEndIndex).let {
            if (it == pendingEndIndex) {
                return startIndex
            }
            pendingEndIndex = it
        }
        return pendingEndIndex
    }

    fun encodeUserInfo(decodedUserInfo: ByteString): String =
        encodePart(decodedUserInfo, CHAR_MASK_USERINFO)

    fun encodeHost(decodedHost: ByteString): String {
        if (
            decodedHost.size > 2 &&
                decodedHost.first() == '['.code.toByte() &&
                decodedHost.last() == ']'.code.toByte()
        ) {
            val encodedIpLiteralAddress =
                encodePart(decodedHost, CHAR_MASK_IP_LITERAL_ADDRESS, 1, decodedHost.lastIndex)
            if (
                parseIpLiteralAddress(encodedIpLiteralAddress, 0) == encodedIpLiteralAddress.length
            ) {
                return "[$encodedIpLiteralAddress]"
            }
        }
        return encodePart(decodedHost, CHAR_MASK_REG_NAME)
    }

    fun encodePath(decodedPath: ByteString): String = encodePart(decodedPath, CHAR_MASK_PATH)

    fun encodeQuery(decodedQuery: ByteString): String = encodePart(decodedQuery, CHAR_MASK_QUERY)

    fun encodeFragment(decodedFragment: ByteString): String =
        encodePart(decodedFragment, CHAR_MASK_FRAGMENT)

    private fun encodePart(
        decodedPart: ByteString,
        charMask: CharMask,
        startIndex: Int = 0,
        endIndex: Int = decodedPart.size
    ): String = buildString {
        for (i in startIndex ..< endIndex) {
            val byte = decodedPart[i]
            val char = byte.toInt().toChar()
            if (charMask.matches(char)) {
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

    fun decodePart(encodedPart: String): ByteString = buildByteString {
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
            else ->
                throw IllegalArgumentException("Non-hex character '$this' in URI percent-encoding")
        }
}
