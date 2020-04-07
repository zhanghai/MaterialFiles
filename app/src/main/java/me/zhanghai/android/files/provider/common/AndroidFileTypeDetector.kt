/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.Path
import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.spi.FileTypeDetector
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.file.forSpecialPosixFileType
import me.zhanghai.android.files.file.guessFromPath
import java.io.IOException

object AndroidFileTypeDetector : FileTypeDetector() {
    @Throws(IOException::class)
    override fun probeContentType(path: Path): String {
        val attributes = path.readAttributes(BasicFileAttributes::class.java)
        return getMimeType(path, attributes)
    }

    fun getMimeType(path: Path, attributes: BasicFileAttributes): String {
        MimeType.forSpecialPosixFileType(attributes.posixFileType)?.let { return it.value }
        if (attributes.isDirectory) {
            return MimeType.DIRECTORY.value
        }
        if (attributes is ContentProviderFileAttributes) {
            attributes.mimeType()?.let { return it }
        }
        return MimeType.guessFromPath(path.toString()).value
    }
}
