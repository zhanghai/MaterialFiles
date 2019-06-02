/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.net.URI;
import java.net.URISyntaxException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ByteStringUriUtils {

    private static final String CHARSET_ALPHA =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String CHARSET_DIGIT = "0123456789";
    private static final String CHARSET_UNRESERVED = CHARSET_ALPHA + CHARSET_DIGIT + "-._~";
    private static final String CHARSET_SUB_DELIMS = "!$&'()*+,;=";
    private static final String CHARSET_PCHAR = CHARSET_UNRESERVED + CHARSET_SUB_DELIMS + ":@";
    private static final String CHARSET_PATH = CHARSET_PCHAR + "/";
    private static final String CHARSET_FRAGMENT = CHARSET_PCHAR + "/?";

    @NonNull
    public static URI createUri(@Nullable String scheme, @Nullable ByteString schemeSpecificPart,
                                @Nullable ByteString fragment) {
        StringBuilder builder = new StringBuilder();
        if (scheme != null) {
            builder
                    .append(scheme)
                    .append(':');
        }
        if (schemeSpecificPart != null) {
            builder.append(encodeSchemeSpecificPart(schemeSpecificPart));
        }
        if (fragment != null) {
            builder.append('#')
                    .append(encodeFragment(fragment));
        }
        String uriString = builder.toString();
        try {
            return new URI(uriString);
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /*
     * @see java.net.URI#appendSchemeSpecificPart
     */
    @NonNull
    private static String encodeSchemeSpecificPart(@NonNull ByteString decoded) {
        if (decoded.length() >= 3 && decoded.byteAt(0) == '/' && decoded.byteAt(1) == '/'
                && decoded.byteAt(2) == '[') {
            int ipLiteralLastCharacterIndex = decoded.indexOf((byte) ']', 3);
            if (ipLiteralLastCharacterIndex == -1) {
                throw new IllegalArgumentException("Incomplete IP literal in URI");
            }
            int ipLiteralEnd = ipLiteralLastCharacterIndex + 1;
            return decoded.substring(0, ipLiteralEnd).toString()
                    + encode(decoded.substring(ipLiteralEnd), CHARSET_PATH);
        } else {
            return encode(decoded, CHARSET_PATH);
        }
    }

    private static String encodeFragment(@NonNull ByteString decoded) {
        return encode(decoded, CHARSET_FRAGMENT);
    }

    @NonNull
    private static String encode(@NonNull ByteString decoded, @NonNull String charset) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, length = decoded.length(); i < length; ++i) {
            byte b = decoded.byteAt(i);
            if (charset.indexOf(b) != -1) {
                builder.append((char) b);
            } else {
                builder
                        .append('%')
                        .append(encodeHexCharacter((byte) ((b >>> 4) & 0x0F)))
                        .append(encodeHexCharacter((byte) (b & 0x0F)));
            }
        }
        return builder.toString();
    }

    private static char encodeHexCharacter(byte halfByte) {
        if (halfByte >= 0 && halfByte <= 9) {
            return (char) ('0' + halfByte);
        } else if (halfByte >= 10 && halfByte <= 15) {
            return (char) ('A' + (halfByte - 10));
        } else {
            throw new IllegalArgumentException("Non-half-byte for percent encoding in URI: "
                    + halfByte);
        }
    }

    @Nullable
    public static ByteString getDecodedSchemeSpecificPart(@NonNull URI uri) {
        return decodeOrNull(uri.getRawSchemeSpecificPart());
    }

    @Nullable
    public static ByteString getDecodedPath(@NonNull URI uri) {
        return decodeOrNull(uri.getRawPath());
    }

    @Nullable
    public static ByteString getDecodedFragment(@NonNull URI uri) {
        return decodeOrNull(uri.getRawFragment());
    }

    @Nullable
    private static ByteString decodeOrNull(@Nullable String encoded) {
        return encoded != null ? decode(encoded) : null;
    }

    @NonNull
    private static ByteString decode(@NonNull String encoded) {
        ByteStringBuilder builder = new ByteStringBuilder();
        int length = encoded.length();
        for (int i = 0; i < length; ) {
            byte b = getAsciiCharacterAt(encoded, i);
            switch (b) {
                case '%':
                    if (i + 3 > length) {
                        throw new IllegalArgumentException("Incomplete percent-encoding in URI");
                    }
                    byte b1 = decodeHexCharacter(getAsciiCharacterAt(encoded, i + 1));
                    byte b2 = decodeHexCharacter(getAsciiCharacterAt(encoded, i + 2));
                    b = (byte) ((b1 << 4) | b2);
                    builder.append(b);
                    i += 3;
                    break;
                default:
                    builder.append(b);
                    ++i;
            }
        }
        return builder.toByteString();
    }

    private static byte getAsciiCharacterAt(@NonNull String string, int index) {
        char c = string.charAt(index);
        if (c != (c & 0x7F)) {
            throw new IllegalArgumentException("Non-ASCII character in URI: " + c);
        }
        return (byte) c;
    }

    private static byte decodeHexCharacter(byte hexCharacter) {
        if (hexCharacter >= '0' && hexCharacter <= '9') {
            return (byte) (hexCharacter - '0');
        } else if (hexCharacter >= 'A' && hexCharacter <= 'F') {
            return (byte) (10 + (hexCharacter - 'A'));
        } else if (hexCharacter >= 'a' && hexCharacter <= 'f') {
            return (byte) (10 + (hexCharacter - 'a'));
        } else {
            throw new IllegalArgumentException("Non-hex-character for percent-encoding in URI: "
                    + (char) hexCharacter);
        }
    }
}
