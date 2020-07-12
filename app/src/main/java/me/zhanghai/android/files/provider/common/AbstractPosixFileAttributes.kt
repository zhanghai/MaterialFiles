/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import android.os.Parcelable
import java8.nio.file.attribute.FileTime

abstract class AbstractPosixFileAttributes : Parcelable, PosixFileAttributes {
    protected abstract val lastModifiedTime: FileTime
    protected abstract val lastAccessTime: FileTime
    protected abstract val creationTime: FileTime
    protected abstract val type: PosixFileType
    protected abstract val size: Long
    protected abstract val fileKey: Parcelable
    protected abstract val owner: PosixUser?
    protected abstract val group: PosixGroup?
    protected abstract val mode: Set<PosixFileModeBit>?
    protected abstract val seLinuxContext: ByteString?

    override fun lastModifiedTime(): FileTime = lastModifiedTime

    override fun lastAccessTime(): FileTime = lastAccessTime

    override fun creationTime(): FileTime = creationTime

    override fun type(): PosixFileType = type

    override fun size(): Long = size

    override fun fileKey(): Parcelable = fileKey

    override fun owner(): PosixUser? = owner

    override fun group(): PosixGroup? = group

    override fun mode(): Set<PosixFileModeBit>? = mode

    override fun seLinuxContext(): ByteString? = seLinuxContext
}
