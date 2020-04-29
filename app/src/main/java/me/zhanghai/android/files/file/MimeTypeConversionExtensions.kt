/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file

import android.webkit.MimeTypeMap
import me.zhanghai.android.files.provider.common.PosixFileType
import me.zhanghai.android.files.util.asFileName
import me.zhanghai.android.files.util.asPathName
import java.util.Locale

// TODO: Use Debian mime.types, as in
//  https://android.googlesource.com/platform/libcore/+/android10-release/luni/src/main/java/libcore/net/mime.types
fun MimeType.Companion.guessFromPath(path: String): MimeType {
    val fileName = path.asPathName().fileName ?: return DIRECTORY
    return guessFromExtension(fileName.asFileName().singleExtension)
}

fun MimeType.Companion.guessFromExtension(extension: String): MimeType {
    val extension = extension.toLowerCase(Locale.US)
    return extensionToMimeTypeMap[extension]
        ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)?.asMimeTypeOrNull()
        ?: GENERIC
}

// See also https://android.googlesource.com/platform/libcore/+/lollipop-release/luni/src/main/java/libcore/net/MimeUtils.java
// See also https://android.googlesource.com/platform/libcore/+/pie-release/luni/src/main/java/libcore/net/MimeUtils.java
// See also http://www.iana.org/assignments/media-types/media-types.xhtml
// See also /usr/share/mime/packages/freedesktop.org.xml
private val extensionToMimeTypeMap = mapOf(
    // Compatibility (starting from L), in the order they appear in Android source.
    "epub" to "application/epub+zip",
    "ogx" to "application/ogg",
    "odp" to "application/vnd.oasis.opendocument.presentation",
    "otp" to "application/vnd.oasis.opendocument.presentation-template",
    "yt" to "application/vnd.youtube.yt",
    "hwp" to "application/x-hwp",
    "3gpp" to "video/3gpp",
    "3gp" to "video/3gpp",
    "3gpp2" to "video/3gpp2",
    "3g2" to "video/3gpp2",
    "oga" to "audio/ogg",
    "ogg" to "audio/ogg",
    "spx" to "audio/ogg",
    "dng" to "image/x-adobe-dng",
    "cr2" to "image/x-canon-cr2",
    "raf" to "image/x-fuji-raf",
    "nef" to "image/x-nikon-nef",
    "nrw" to "image/x-nikon-nrw",
    "orf" to "image/x-olympus-orf",
    "rw2" to "image/x-panasonic-rw2",
    "pef" to "image/x-pentax-pef",
    "srw" to "image/x-samsung-srw",
    "arw" to "image/x-sony-arw",
    "ogv" to "video/ogg",
    // Fixes
    "tgz" to "application/x-gtar-compressed", // Was "application/x-gtar"
    "taz" to "application/x-gtar-compressed", // Was "application/x-gtar"
    "csv" to "text/csv", // Was "text/comma-separated-values"
    // Addition
    "gz" to "application/gzip",
    "cab" to "application/vnd.ms-cab-compressed",
    "7z" to "application/x-7z-compressed",
    "bz" to "application/x-bzip",
    "bz2" to "application/x-bzip2",
    "z" to "application/x-compress",
    "jar" to "application/x-java-archive",
    "lzma" to "application/x-lzma",
    "xz" to "application/x-xz",
    "m3u" to "audio/x-mpegurl",
    "m3u8" to "audio/x-mpegurl",
    "p7b" to "application/x-pkcs7-certificates",
    "spc" to "application/x-pkcs7-certificates",
    "p7c" to "application/pkcs7-mime",
    "p7s" to "application/pkcs7-signature",
    "es" to "application/ecmascript",
    "js" to "application/javascript",
    "json" to "application/json",
    "ts" to "application/typescript", // Clashes with "video/mp2ts"
    "perl" to "text/x-perl",
    "pl" to "text/x-perl",
    "pm" to "text/x-perl",
    "py" to "text/x-python",
    "py3" to "text/x-python",
    "py3x" to "text/x-python",
    "pyx" to "text/x-python",
    "wsgi" to "text/x-python",
    "rb" to "text/ruby",
    "sh" to "application/x-sh",
    "yaml" to "text/x-yaml",
    "yml" to "text/x-yaml",
    "asm" to "text/x-asm",
    "s" to "text/x-asm",
    "cs" to "text/x-csharp",
    "azw" to "application/vnd.amazon.ebook",
    "ibooks" to "application/x-ibooks+zip",
    "mobi" to "application/x-mobipocket-ebook",
    "woff" to "font/woff",
    "woff2" to "font/woff2",
    "msg" to "application/vnd.ms-outlook",
    "eml" to "message/rfc822",
    "eot" to "application/vnd.ms-fontobject",
    "ttf" to "font/ttf",
    "otf" to "font/otf",
    "ttc" to "font/collection",
    "markdown" to "text/markdown",
    "md" to "text/markdown",
    "mkd" to "text/markdown",
    "conf" to "text/plain",
    "ini" to "text/plain",
    "list" to "text/plain",
    "log" to "text/plain",
    "prop" to "text/plain",
    "properties" to "text/plain",
    "rc" to "text/plain",
    "flv" to "video/x-flv"
).mapValues { it.value.asMimeType() }

fun MimeType.Companion.forSpecialPosixFileType(type: PosixFileType): MimeType? =
    specialPosixFileTypeToMimeTypeMap[type]

// See also https://developer.gnome.org/shared-mime-info-spec/
/** @see specialPosixFileTypeToNameResMap */
private val specialPosixFileTypeToMimeTypeMap = mapOf(
    PosixFileType.CHARACTER_DEVICE to "inode/chardevice",
    PosixFileType.BLOCK_DEVICE to "inode/blockdevice",
    PosixFileType.FIFO to "inode/fifo",
    PosixFileType.SYMBOLIC_LINK to "inode/symlink",
    PosixFileType.SOCKET to "inode/socket"
).mapValues { it.value.asMimeType() }

val MimeType.intentType: String
    get() = intentMimeType.value

private val MimeType.intentMimeType: MimeType
    get() = mimeTypeToIntentMimeTypeMap[this] ?: this

private val mimeTypeToIntentMimeTypeMap = listOf(
    // Allows matching "text/*"
    "application/ecmascript" to "text/ecmascript",
    "application/javascript" to "text/javascript",
    "application/json" to "text/json",
    "application/typescript" to "text/typescript",
    "application/x-sh" to "text/x-shellscript",
    "application/x-shellscript" to "text/x-shellscript",
    // Allows matching generic
    MimeType.GENERIC.value to MimeType.ANY.value
).associate { it.first.asMimeType() to it.second.asMimeType() }

val Collection<MimeType>.intentType: String
    get() {
        if (isEmpty()) {
            return MimeType.ANY.value
        }
        val intentMimeTypes = map { it.intentMimeType }
        val firstIntentMimeType = intentMimeTypes.first()
        if (intentMimeTypes.all { firstIntentMimeType.match(it) }) {
            return firstIntentMimeType.value
        }
        val wildcardIntentMimeType = MimeType.of(firstIntentMimeType.type, "*", null)
        if (intentMimeTypes.all { wildcardIntentMimeType.match(it) }) {
            return wildcardIntentMimeType.value
        }
        return MimeType.ANY.value
    }
