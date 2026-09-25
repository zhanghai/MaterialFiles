/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.mediastore

import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.attribute.FileTime

class MediaStoreRootAttributes : BasicFileAttributes {
    override fun lastModifiedTime(): FileTime = FileTime.fromMillis(0)
    override fun lastAccessTime(): FileTime = FileTime.fromMillis(0)
    override fun creationTime(): FileTime = FileTime.fromMillis(0)
    override fun isRegularFile(): Boolean = false
    override fun isDirectory(): Boolean = true
    override fun isSymbolicLink(): Boolean = false
    override fun isOther(): Boolean = false
    override fun size(): Long = 0
    override fun fileKey(): Any? = null
}
