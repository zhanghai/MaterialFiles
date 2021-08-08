/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file

import android.os.Build

val MimeType.isApk: Boolean
    get() = this == MimeType.APK

val MimeType.isSupportedArchive: Boolean
    get() = this in supportedArchiveMimeTypes

private val supportedArchiveMimeTypes = mutableListOf(
    "application/gzip",
    "application/java-archive",
    "application/rar",
    "application/zip",
    "application/vnd.android.package-archive",
    "application/vnd.debian.binary-package",
    "application/x-bzip2",
    "application/x-compress",
    "application/x-cpio",
    "application/x-deb",
    "application/x-debian-package",
    "application/x-gtar",
    "application/x-gtar-compressed",
    "application/x-java-archive",
    "application/x-lzma",
    "application/x-tar",
    "application/x-xz"
)
    .apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this += "application/x-7z-compressed"
        }
    }
    .map { it.asMimeType() }.toSet()

val MimeType.isImage: Boolean
    get() = icon == MimeTypeIcon.IMAGE

val MimeType.isAudio: Boolean
    get() = icon == MimeTypeIcon.AUDIO

val MimeType.isVideo: Boolean
    get() = icon == MimeTypeIcon.VIDEO

val MimeType.isMedia: Boolean
    get() = isAudio || isVideo

val MimeType.isPdf: Boolean
    get() = this == MimeType.PDF
