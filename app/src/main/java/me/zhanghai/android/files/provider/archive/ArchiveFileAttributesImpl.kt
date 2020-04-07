/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive

import java8.nio.file.Path
import java8.nio.file.attribute.FileTime
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.PosixFileAttributes
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

internal class ArchiveFileAttributesImpl(
    private val archiveFile: Path,
    private val entry: ArchiveEntry
) : PosixFileAttributes {
    val entryName: String
        get() = entry.name

    override fun lastModifiedTime(): FileTime =
        FileTime.from(Instant.ofEpochMilli(entry.lastModifiedDate.time))

    override fun lastAccessTime(): FileTime {
        when (entry) {
            is DumpArchiveEntry -> return FileTime.from(Instant.ofEpochMilli(entry.accessTime.time))
            is SevenZArchiveEntry ->
                if (entry.hasAccessDate) {
                    return FileTime.from(Instant.ofEpochMilli(entry.accessDate.time))
                }
            is TarArchiveEntry -> {
                val atimeMillis = entry.getExtraPaxHeaderTimeMillis("atime")
                if (atimeMillis != null) {
                    return FileTime.from(Instant.ofEpochMilli(atimeMillis))
                }
            }
        }
        return lastModifiedTime()
    }

    override fun creationTime(): FileTime {
        when (entry) {
            is DumpArchiveEntry ->
                return FileTime.from(Instant.ofEpochMilli(entry.creationTime.time))
            is SevenZArchiveEntry ->
                if (entry.hasCreationDate) {
                    return FileTime.from(Instant.ofEpochMilli(entry.creationDate.time))
                }
            is TarArchiveEntry -> {
                val ctimeMillis = entry.getExtraPaxHeaderTimeMillis("ctime")
                if (ctimeMillis != null) {
                    return FileTime.from(Instant.ofEpochMilli(ctimeMillis))
                }
            }
        }
        return lastModifiedTime()
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

    override fun type(): PosixFileType = entry.posixFileType

    override fun size(): Long = entry.size

    override fun fileKey(): ArchiveFileKey = ArchiveFileKey(archiveFile, entry.name)

    override fun owner(): PosixUser? =
        when (entry) {
            is DumpArchiveEntry -> PosixUser(entry.userId, null)
            is TarArchiveEntry -> PosixUser(entry.userId, entry.userName?.toByteString())
            else -> null
        }

    override fun group(): PosixGroup? =
        when (entry) {
            is DumpArchiveEntry -> PosixGroup(entry.groupId, null)
            is TarArchiveEntry -> PosixGroup(entry.groupId, entry.groupName?.toByteString())
            else -> null
        }

    override fun mode(): Set<PosixFileModeBit>? =
        when (entry) {
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

    override fun seLinuxContext(): ByteString? = null
}
