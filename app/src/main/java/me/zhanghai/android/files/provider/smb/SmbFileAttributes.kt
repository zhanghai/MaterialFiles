/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb

import android.os.Parcelable
import com.hierynomus.msfscc.FileAttributes
import java8.nio.file.attribute.FileTime
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import me.zhanghai.android.files.provider.common.AbstractBasicFileAttributes
import me.zhanghai.android.files.provider.common.BasicFileType
import me.zhanghai.android.files.provider.common.FileTimeParceler
import me.zhanghai.android.files.provider.smb.client.FileInformation
import me.zhanghai.android.files.util.hasBits
import org.threeten.bp.Instant

@Parcelize
internal class SmbFileAttributes(
    override val lastModifiedTime: @WriteWith<FileTimeParceler> FileTime,
    override val lastAccessTime: @WriteWith<FileTimeParceler> FileTime,
    override val creationTime: @WriteWith<FileTimeParceler> FileTime,
    override val type: BasicFileType,
    override val size: Long,
    override val fileKey: Parcelable,
    private val attributes: Long
) : AbstractBasicFileAttributes() {
    fun attributes(): Long = attributes

    companion object {
        fun from(fileInformation: FileInformation, path: SmbPath): SmbFileAttributes {
            // lastWriteTime is returned by GetFileTime(), while changeTime isn't returned.
            // https://docs.microsoft.com/zh-cn/windows/win32/api/fileapi/nf-fileapi-getfiletime
            val lastModifiedTime =
                FileTime.from(Instant.ofEpochMilli(fileInformation.lastWriteTime.toEpochMillis()))
            val lastAccessTime =
                FileTime.from(Instant.ofEpochMilli(fileInformation.lastAccessTime.toEpochMillis()))
            val creationTime =
                FileTime.from(Instant.ofEpochMilli(fileInformation.creationTime.toEpochMillis()))
            val attributes = fileInformation.fileAttributes
            val type = when {
                attributes.hasBits(FileAttributes.FILE_ATTRIBUTE_REPARSE_POINT.value) ->
                    BasicFileType.SYMBOLIC_LINK
                attributes.hasBits(FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value) ->
                    BasicFileType.DIRECTORY
                else -> BasicFileType.REGULAR_FILE
            }
            val size = fileInformation.endOfFile
            val fileKey = SmbFileKey(path, fileInformation.fileId)
            return SmbFileAttributes(
                lastModifiedTime, lastAccessTime, creationTime, type, size, fileKey, attributes
            )
        }
    }
}
