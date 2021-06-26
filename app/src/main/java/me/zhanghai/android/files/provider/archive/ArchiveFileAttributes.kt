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
import me.zhanghai.android.files.provider.common.AbstractPosixFileAttributes
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.FileTimeParceler
import me.zhanghai.android.files.provider.common.PosixFileMode
import me.zhanghai.android.files.provider.common.PosixFileModeBit
import me.zhanghai.android.files.provider.common.PosixFileType
import me.zhanghai.android.files.provider.common.PosixGroup
import me.zhanghai.android.files.provider.common.PosixUser
import me.zhanghai.android.files.provider.common.posixFileType
import me.zhanghai.android.files.provider.common.toByteString
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.dump.DumpArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.threeten.bp.Instant

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
    private val entryName: String
) : AbstractPosixFileAttributes() {
    fun entryName(): String = entryName

    companion object {
        fun from(archiveFile: Path, entry: ArchiveEntry): ArchiveFileAttributes {
            val lastModifiedTime = FileTime.from(Instant.ofEpochMilli(entry.lastModifiedDate.time))
            val lastAccessTime = when (entry) {
                is DumpArchiveEntry -> FileTime.from(Instant.ofEpochMilli(entry.accessTime.time))
                is SevenZArchiveEntry ->
                    if (entry.hasAccessDate) {
                        FileTime.from(Instant.ofEpochMilli(entry.accessDate.time))
                    } else {
                        lastModifiedTime
                    }
                is TarArchiveEntry -> {
                    val atimeMillis = entry.getExtraPaxHeaderTimeMillis("atime")
                    if (atimeMillis != null) {
                        FileTime.from(Instant.ofEpochMilli(atimeMillis))
                    } else {
                        lastModifiedTime
                    }
                }
                else -> lastModifiedTime
            }
            val creationTime = when (entry) {
                is DumpArchiveEntry -> FileTime.from(Instant.ofEpochMilli(entry.creationTime.time))
                is SevenZArchiveEntry ->
                    if (entry.hasCreationDate) {
                        FileTime.from(Instant.ofEpochMilli(entry.creationDate.time))
                    } else {
                        lastModifiedTime
                    }
                is TarArchiveEntry -> {
                    val ctimeMillis = entry.getExtraPaxHeaderTimeMillis("ctime")
                    if (ctimeMillis != null) {
                        FileTime.from(Instant.ofEpochMilli(ctimeMillis))
                    } else {
                        lastModifiedTime
                    }
                }
                else -> lastModifiedTime
            }
            val type = entry.posixFileType
            val size = entry.size
            val fileKey = ArchiveFileKey(archiveFile, entry.name)
            val owner = when (entry) {
                is DumpArchiveEntry -> PosixUser(entry.userId, null)
                is TarArchiveEntry ->
                    @Suppress("DEPRECATION")
                    PosixUser(entry.userId, entry.userName?.toByteString())
                else -> null
            }
            val group = when (entry) {
                is DumpArchiveEntry -> PosixGroup(entry.groupId, null)
                is TarArchiveEntry ->
                    @Suppress("DEPRECATION")
                    PosixGroup(entry.groupId, entry.groupName?.toByteString())
                else -> null
            }
            val mode = when (entry) {
                is DumpArchiveEntry -> PosixFileMode.fromInt(entry.mode)
                is TarArchiveEntry -> PosixFileMode.fromInt(entry.mode)
                is ZipArchiveEntry ->
                    if (entry.platform == ZipArchiveEntry.PLATFORM_UNIX) {
                        PosixFileMode.fromInt(entry.unixMode)
                    } else {
                        null
                    }
                else -> null
            }
            val seLinuxContext = null
            val entryName = entry.name
            return ArchiveFileAttributes(
                lastModifiedTime, lastAccessTime, creationTime, type, size, fileKey, owner, group,
                mode, seLinuxContext, entryName
            )
        }

        private fun TarArchiveEntry.getExtraPaxHeaderTimeMillis(name: String): Long? {
            val timeString = getExtraPaxHeader(name) ?: return null
            val timeSeconds = try {
                timeString.toDouble()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                return null
            }
            return (timeSeconds * 1000).toLong()
        }
    }
}
