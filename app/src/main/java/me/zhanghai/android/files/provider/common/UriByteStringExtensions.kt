/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java.net.URI
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.reflect.KClass

// Note: The URI must have an authority, otherwise the Java URI class recognizes the rest of the URI
// as scheme specific part and refuses to parse our query. As a result, our path must also either be
// empty or absolute (beginning with a slash character).
// @see https://datatracker.ietf.org/doc/html/rfc3986
fun KClass<URI>.create(
    scheme: String,
    authority: UriAuthority,
    path: ByteString,
    query: ByteString?
): URI {
    val builder = StringBuilder()
    builder.append(scheme).append(':')
    builder.append("//").append(authority.encode())
    require(path.isEmpty() || path.startsWith("/".toByteString())) {
        "Path $path must either be empty or begin with a slash character"
    }
    builder.append(encodePath(path))
    if (query != null) {
        builder.append('?').append(encodeQuery(query))
    }
    val uriString = builder.toString()
    return URI.create(uriString)
}

// @see java.net.URI
private const val CHARSET_ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
private const val CHARSET_DIGIT = "0123456789"
private const val CHARSET_UNRESERVED = "$CHARSET_ALPHA$CHARSET_DIGIT-._~"
private const val CHARSET_SUB_DELIMS = "!$&'()*+,;="
private const val CHARSET_PCHAR = "$CHARSET_UNRESERVED$CHARSET_SUB_DELIMS:@"
private const val CHARSET_PATH = "$CHARSET_PCHAR/"
private const val CHARSET_QUERY = "$CHARSET_PCHAR/?"

private fun encodePath(decoded: ByteString): String = encode(decoded, CHARSET_PATH)

private fun encodeQuery(decoded: ByteString): String = encode(decoded, CHARSET_QUERY)

private fun encode(decoded: ByteString, charset: String): String {
    val builder = StringBuilder()
    for (byte in decoded) {
        if (charset.indexOf(byte.toInt().toChar()) != -1) {
            builder.append(byte.toInt().toChar())
        } else {
            builder
                .append('%')
                .append(encodeHexCharacter(((byte.toInt() ushr 4).toByte() and 0x0F)))
                .append(encodeHexCharacter(byte and 0x0F))
        }
    }
    return builder.toString()
}

private fun encodeHexCharacter(halfByte: Byte): Char =
    when (halfByte) {
        in 0..9 -> '0' + halfByte.toInt()
        in 10..15 -> 'A' + (halfByte.toInt() - 10)
        else ->
            throw IllegalArgumentException("Non-half-byte $halfByte for percent-encoding in URI")
    }

val URI.decodedPathByteString: ByteString?
    get() = rawPath?.let { decode(it) }

val URI.decodedQueryByteString: ByteString?
    get() = rawQuery?.let { decode(it) }

private fun decode(encoded: String): ByteString {
    val builder = ByteStringBuilder()
    var index = 0
    val length = encoded.length
    while (index < length) {
        var byte = getAsciiCharacterAt(encoded, index)
        when (byte) {
            '%'.code.toByte() -> {
                require(index + 3 <= length) { "Incomplete percent-encoding in URI" }
                val halfByte1 = decodeHexCharacter(getAsciiCharacterAt(encoded, index + 1))
                val halfByte2 = decodeHexCharacter(getAsciiCharacterAt(encoded, index + 2))
                byte = (halfByte1.toInt() shl 4).toByte() or halfByte2
                builder.append(byte)
                index += 3
            }
            else -> {
                builder.append(byte)
                ++index
            }
        }
    }
    return builder.toByteString()
}

private fun getAsciiCharacterAt(string: String, index: Int): Byte {
    val char = string[index]
    require(char.code == char.code and 0x7F) { "Non-ASCII character $char in URI" }
    return char.code.toByte()
}

private fun decodeHexCharacter(hexCharacter: Byte): Byte =
    when (hexCharacter) {
        in '0'.code.toByte()..'9'.code.toByte() -> (hexCharacter.toInt().toChar() - '0').toByte()
        in 'A'.code.toByte()..'F'.code.toByte() ->
            (10 + (hexCharacter.toInt().toChar() - 'A')).toByte()
        in 'a'.code.toByte()..'f'.code.toByte() ->
            (10 + (hexCharacter.toInt().toChar() - 'a')).toByte()
        else ->
            throw IllegalArgumentException(
                "Non-hex-character ${hexCharacter.toInt().toChar()} for percent-encoding in URI"
            )
    }
