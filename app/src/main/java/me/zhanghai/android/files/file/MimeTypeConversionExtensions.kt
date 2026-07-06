/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file

import android.webkit.MimeTypeMap
import me.zhanghai.android.files.compat.getMimeTypeFromExtensionCompat
import me.zhanghai.android.files.provider.common.PosixFileType
import me.zhanghai.android.files.util.asFileName
import me.zhanghai.android.files.util.asPathName

fun MimeType.Companion.guessFromPath(path: String): MimeType {
    val fileName = path.asPathName().fileName ?: return DIRECTORY
    return guessFromExtension(fileName.asFileName().singleExtension)
}

fun MimeType.Companion.guessFromExtension(extension: String): MimeType {
    val extension = extension.lowercase()
    return extensionToMimeTypeOverrideMap[extension]
        ?: MimeTypeMap.getSingleton().getMimeTypeFromExtensionCompat(extension)?.asMimeTypeOrNull()
        ?: GENERIC
}

// @see https://android.googlesource.com/platform/external/mime-support/+/master/mime.types
// @see https://android.googlesource.com/platform/frameworks/base/+/master/mime/java-res/android.mime.types
// @see http://www.iana.org/assignments/media-types/media-types.xhtml
// @see https://salsa.debian.org/debian/media-types/-/blob/master/mime.types
// @see /usr/share/mime/packages/freedesktop.org.xml
private val extensionToMimeTypeOverrideMap = mapOf(
    // Fixes
    "csv" to "text/csv", // Was "text/comma-separated-values"
    "sh" to "application/x-sh", // Was "text/x-sh"
    // Addition
    "bz" to "application/x-bzip",
    "bz2" to "application/x-bzip2",
    "z" to "application/x-compress",
    "lzma" to "application/x-lzma",
    "p7b" to "application/x-pkcs7-certificates",
    "spc" to "application/x-pkcs7-certificates", // Clashes with "chemical/x-galactic-spc"
    "ts" to "application/typescript", // Clashes with "video/mp2ts"
    "py3" to "text/x-python",
    "py3x" to "text/x-python",
    "pyx" to "text/x-python",
    "wsgi" to "text/x-python",
    "yml" to "application/yaml",
    "asm" to "text/x-asm",
    "s" to "text/x-asm",
    "cs" to "text/x-csharp",
    "azw" to "application/vnd.amazon.ebook",
    "ibooks" to "application/x-ibooks+zip",
    "msg" to "application/vnd.ms-outlook",
    "mkd" to "text/markdown",
    "conf" to "text/plain",
    "ini" to "text/plain",
    "list" to "text/plain",
    "log" to "text/plain",
    "prop" to "text/plain",
    "properties" to "text/plain",
    "rc" to "text/plain",
    "md" to "text/markdown",
    "markdown" to "text/markdown",
    "mdown" to "text/markdown",
    "kt" to "text/x-kotlin",
    "ktm" to "text/x-kotlin",
    "kts" to "text/x-kotlin",
    "gradle" to "text/x-gradle",
    "gradle.kts" to "text/x-kotlin",
    "jsx" to "text/jsx",
    "tsx" to "text/typescript",
    "mjs" to "application/javascript",
    "cjs" to "application/javascript",
    "scss" to "text/x-scss",
    "sass" to "text/x-sass",
    "less" to "text/x-less",
    "styl" to "text/x-stylus",
    "toml" to "text/toml",
    "php" to "text/x-php",
    "rb" to "text/x-ruby",
    "go" to "text/x-go",
    "rs" to "text/x-rust",
    "swift" to "text/x-swift",
    "c" to "text/x-c",
    "cpp" to "text/x-c++",
    "cxx" to "text/x-c++",
    "h" to "text/x-c",
    "hpp" to "text/x-c++",
    "sql" to "text/x-sql",
    "r" to "text/x-r",
    "m" to "text/x-objective-c",
    "mm" to "text/x-objective-c",
    "pl" to "text/x-perl",
    "pm" to "text/x-perl",
    "lua" to "text/x-lua",
    "dart" to "text/x-dart",
    "groovy" to "text/x-groovy",
    "bat" to "text/x-bat",
    "cmd" to "text/x-bat",
    "ps1" to "text/x-powershell",
    "psm1" to "text/x-powershell",
    "diff" to "text/x-diff",
    "patch" to "text/x-diff",
    "proto" to "text/x-protobuf",
    "graphql" to "text/x-graphql",
    "gql" to "text/x-graphql",
    "dockerfile" to "text/x-dockerfile",
    "makefile" to "text/x-makefile",
    "cmake" to "text/x-cmake",
    "editorconfig" to "text/plain",
    "gitignore" to "text/plain",
    "gitattributes" to "text/plain",
    "asc" to "text/plain",
    "nfo" to "text/plain",
    "env" to "text/plain"
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

val MimeType.extension: String?
    // TODO: Add compat implementation as well.
    get() = MimeTypeMap.getSingleton().getExtensionFromMimeType(value)

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
    "application/yaml" to "text/x-yaml",
    "application/x-sh" to "text/x-shellscript",
    "application/x-shellscript" to "text/x-shellscript",
    // Allows matching generic
    MimeType.GENERIC.value to MimeType.ANY.value
).associate { it.first.asMimeType() to it.second.asMimeType() }

val MimeType.isPreviewable: Boolean
    get() = this in previewableMimeTypes

private val previewableMimeTypes = listOf(
    "text/markdown",
    "text/x-kotlin",
    "text/x-gradle",
    "text/x-python",
    "text/x-php",
    "text/x-ruby",
    "text/x-go",
    "text/x-rust",
    "text/x-swift",
    "text/x-c",
    "text/x-c++",
    "text/x-objective-c",
    "text/x-perl",
    "text/x-lua",
    "text/x-dart",
    "text/x-groovy",
    "text/x-bat",
    "text/x-powershell",
    "text/x-diff",
    "text/x-protobuf",
    "text/x-graphql",
    "text/x-dockerfile",
    "text/x-makefile",
    "text/x-cmake",
    "text/x-java",
    "text/x-php",
    "text/x-asm",
    "text/x-csharp",
    "text/x-sql",
    "text/x-r",
    "text/x-scss",
    "text/x-sass",
    "text/x-less",
    "text/x-stylus",
    "text/toml",
    "text/jsx",
    "text/typescript",
    "application/javascript",
    "application/json",
    "application/typescript",
    "application/x-sh",
    "application/x-shellscript",
    "application/yaml",
    "application/xml",
    "application/ecmascript"
).map { it.asMimeType() }.toSet()

val MimeType.isMarkdown: Boolean
    get() = this == "text/markdown".asMimeType()

val MimeType.isCode: Boolean
    get() = isPreviewable && !isMarkdown && this != MimeType.TEXT_PLAIN

private val Collection<MimeType>.intentType: String
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
