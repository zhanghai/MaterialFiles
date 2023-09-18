/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive

import android.os.Parcelable
import java8.nio.file.Path
import java8.nio.file.attribute.FileTime
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import me.zhanghai.android.files.provider.archive.archiver.ReadArchive
import me.zhanghai.android.files.provider.common.AbstractPosixFileAttributes
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.EncryptedFileAttributes
import me.zhanghai.android.files.provider.common.FileTimeParceler
import me.zhanghai.android.files.provider.common.PosixFileModeBit
import me.zhanghai.android.files.provider.common.PosixFileType
import me.zhanghai.android.files.provider.common.PosixGroup
import me.zhanghai.android.files.provider.common.PosixUser

@Parcelize
internal class ArchiveFileAttributes(
    override val lastModifiedTime: @WriteWith<FileTimeParceler> FileTime,
    override val lastAccessTime: @WriteWith<FileTimeParceler> FileTime,
    override val creationTime: @WriteWith<FileTimeParceler> FileTime,
    override val type: PosixFileType,
    override val size: Long,
    override val fileKey: Parcelable,
    override val owner: PosixUser?,
    override val group: PosixGroup?,
    override val mode: Set<PosixFileModeBit>?,
    override val seLinuxContext: ByteString?,
    private val isEncrypted: Boolean,
    private val entryName: String
) : AbstractPosixFileAttributes(), EncryptedFileAttributes {
    override fun isEncrypted(): Boolean = isEncrypted

    fun entryName(): String = entryName

    companion object {
        fun from(archiveFile: Path, entry: ReadArchive.Entry): ArchiveFileAttributes {
            val lastModifiedTime = entry.lastModifiedTime ?: FileTime.fromMillis(0)
            val lastAccessTime = entry.lastAccessTime ?: lastModifiedTime
            val creationTime = entry.creationTime ?: lastModifiedTime
            val type = entry.type
            val size = entry.size
            val fileKey = ArchiveFileKey(archiveFile, entry.name)
            val owner = entry.owner
            val group = entry.group
            val mode = entry.mode
            val seLinuxContext = null
            val isEncrypted = entry.isEncrypted
            val entryName = entry.name
            return ArchiveFileAttributes(
                lastModifiedTime, lastAccessTime, creationTime, type, size, fileKey, owner, group,
                mode, seLinuxContext, isEncrypted, entryName
            )
        }
    }
}
