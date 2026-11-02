/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.rclone

import android.os.Parcelable
import java.time.Instant
import java8.nio.file.attribute.FileTime
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import me.zhanghai.android.files.provider.common.AbstractBasicFileAttributes
import me.zhanghai.android.files.provider.common.BasicFileType
import me.zhanghai.android.files.provider.common.EPOCH
import me.zhanghai.android.files.provider.common.FileTimeParceler
import me.zhanghai.android.files.provider.rclone.client.RcloneItem

@Parcelize
internal data class RcloneFileAttributes(
    override val lastModifiedTime: @WriteWith<FileTimeParceler> FileTime,
    override val lastAccessTime: @WriteWith<FileTimeParceler> FileTime,
    override val creationTime: @WriteWith<FileTimeParceler> FileTime,
    override val type: BasicFileType,
    override val size: Long,
    override val fileKey: Parcelable
) : AbstractBasicFileAttributes() {
    companion object {
        fun from(item: RcloneItem, path: RclonePath): RcloneFileAttributes {
            val lastModifiedTime = item.modTime?.let {
                try {
                    FileTime.from(Instant.parse(it))
                } catch (e: Exception) {
                    FileTime::class.EPOCH
                }
            } ?: FileTime::class.EPOCH
            val type = if (item.isDir) BasicFileType.DIRECTORY else BasicFileType.REGULAR_FILE
            val size = if (item.isDir) 0L else item.size.coerceAtLeast(0L)
            return RcloneFileAttributes(
                lastModifiedTime, lastModifiedTime, lastModifiedTime, type, size, path
            )
        }

        fun rootDirectory(path: RclonePath): RcloneFileAttributes {
            val epoch = FileTime::class.EPOCH
            return RcloneFileAttributes(epoch, epoch, epoch, BasicFileType.DIRECTORY, 0L, path)
        }
    }
}
