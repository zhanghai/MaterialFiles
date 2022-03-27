/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.ftp

import java8.nio.file.LinkOption
import java8.nio.file.attribute.BasicFileAttributeView
import java8.nio.file.attribute.FileTime
import me.zhanghai.android.files.provider.ftp.client.Client
import java.io.IOException

internal class FtpFileAttributeView(
    private val path: FtpPath,
    private val noFollowLinks: Boolean
) : BasicFileAttributeView {
    override fun name(): String = NAME

    @Throws(IOException::class)
    override fun readAttributes(): FtpFileAttributes {
        val file = try {
            Client.listFile(path, noFollowLinks)
        } catch (e: IOException) {
            throw e.toFileSystemExceptionForFtp(path.toString())
        }
        return FtpFileAttributes.from(file, path)
    }

    override fun setTimes(
        lastModifiedTime: FileTime?,
        lastAccessTime: FileTime?,
        createTime: FileTime?
    ) {
        if (lastModifiedTime == null) {
            // Only throw if caller is trying to set only last access time and/or create time, so
            // that foreign copy move can still set last modified time.
            if (lastAccessTime != null) {
                throw UnsupportedOperationException("lastAccessTime")
            }
            if (createTime != null) {
                throw UnsupportedOperationException("createTime")
            }
            return
        }
        if (noFollowLinks) {
            throw UnsupportedOperationException(LinkOption.NOFOLLOW_LINKS.toString())
        }
        try {
            Client.setLastModifiedTime(path, lastModifiedTime.toInstant())
        } catch (e: IOException) {
            throw e.toFileSystemExceptionForFtp(path.toString())
        }
    }

    companion object {
        private val NAME = FtpFileSystemProvider.scheme

        val SUPPORTED_NAMES = setOf("basic", NAME)
    }
}
