/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.rclone

import java8.nio.file.NoSuchFileException
import java8.nio.file.attribute.BasicFileAttributeView
import java8.nio.file.attribute.FileTime
import me.zhanghai.android.files.provider.rclone.client.RcloneClient
import java.io.IOException

internal class RcloneFileAttributeView(
    private val path: RclonePath
) : BasicFileAttributeView {
    override fun name(): String = NAME

    @Throws(IOException::class)
    override fun readAttributes(): RcloneFileAttributes {
        val item = RcloneClient.stat(path.remoteName, path.remotePath)
            ?: throw NoSuchFileException(path.toString())
        return RcloneFileAttributes.from(item, path)
    }

    override fun setTimes(
        lastModifiedTime: FileTime?,
        lastAccessTime: FileTime?,
        createTime: FileTime?
    ) {
        // Setting times is not strictly required by rclone operations.
    }

    companion object {
        private val NAME = RcloneFileSystemProvider.scheme
        val SUPPORTED_NAMES = setOf("basic", NAME)
    }
}
