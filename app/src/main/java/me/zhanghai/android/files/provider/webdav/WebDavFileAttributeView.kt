/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.webdav

import at.bitfire.dav4jvm.exception.DavException
import java8.nio.file.LinkOption
import java8.nio.file.attribute.BasicFileAttributeView
import java8.nio.file.attribute.FileTime
import me.zhanghai.android.files.provider.webdav.client.Client
import java.io.IOException

internal class WebDavFileAttributeView(
    private val path: WebDavPath,
    private val noFollowLinks: Boolean
) : BasicFileAttributeView {
    override fun name(): String = NAME

    @Throws(IOException::class)
    override fun readAttributes(): WebDavFileAttributes {
        val file = try {
            Client.findProperties(path, noFollowLinks)
        } catch (e: DavException) {
            throw e.toFileSystemException(path.toString())
        }
        return WebDavFileAttributes.from(file, path)
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
        } catch (e: DavException) {
            throw e.toFileSystemException(path.toString())
        }
    }

    companion object {
        private val NAME = WebDavFileSystemProvider.scheme

        val SUPPORTED_NAMES = setOf("basic", NAME)
    }
}
