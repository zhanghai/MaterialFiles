/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import android.os.Parcelable
import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.attribute.FileTime

abstract class AbstractBasicFileAttributes : BasicFileAttributes, Parcelable {
    protected abstract val lastModifiedTime: FileTime
    protected abstract val lastAccessTime: FileTime
    protected abstract val creationTime: FileTime
    protected abstract val type: BasicFileType
    protected abstract val size: Long
    protected abstract val fileKey: Parcelable

    override fun lastModifiedTime(): FileTime = lastModifiedTime

    override fun lastAccessTime(): FileTime = lastAccessTime

    override fun creationTime(): FileTime = creationTime

    override fun isRegularFile(): Boolean = type == BasicFileType.REGULAR_FILE

    override fun isDirectory(): Boolean = type == BasicFileType.DIRECTORY

    override fun isSymbolicLink(): Boolean = type == BasicFileType.SYMBOLIC_LINK

    override fun isOther(): Boolean = type == BasicFileType.OTHER

    override fun size(): Long = size

    override fun fileKey(): Parcelable? = fileKey
}

enum class BasicFileType {
    REGULAR_FILE,
    DIRECTORY,
    SYMBOLIC_LINK,
    OTHER
}
