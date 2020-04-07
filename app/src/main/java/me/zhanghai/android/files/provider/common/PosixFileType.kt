/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import android.system.OsConstants
import java8.nio.file.attribute.BasicFileAttributes
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.dump.DumpArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry

// https://www.gnu.org/software/libc/manual/html_node/Testing-File-Type.html
enum class PosixFileType {
    UNKNOWN,
    DIRECTORY,
    CHARACTER_DEVICE,
    BLOCK_DEVICE,
    REGULAR_FILE,
    FIFO,
    SYMBOLIC_LINK,
    SOCKET;

    companion object {
        fun fromMode(mode: Int): PosixFileType =
            when {
                OsConstants.S_ISDIR(mode) -> DIRECTORY
                OsConstants.S_ISCHR(mode) -> CHARACTER_DEVICE
                OsConstants.S_ISBLK(mode) -> BLOCK_DEVICE
                OsConstants.S_ISREG(mode) -> REGULAR_FILE
                OsConstants.S_ISFIFO(mode) -> FIFO
                OsConstants.S_ISLNK(mode) -> SYMBOLIC_LINK
                OsConstants.S_ISSOCK(mode) -> SOCKET
                else -> UNKNOWN
            }
    }
}

val ArchiveEntry.posixFileType: PosixFileType
    get() =
        when (this) {
            is DumpArchiveEntry -> posixFileType
            is TarArchiveEntry -> posixFileType
            is ZipArchiveEntry -> posixFileType
            else -> if (isDirectory) PosixFileType.DIRECTORY else PosixFileType.REGULAR_FILE
        }

private val DumpArchiveEntry.posixFileType: PosixFileType
    get() =
        when (type) {
            DumpArchiveEntry.TYPE.SOCKET -> PosixFileType.SOCKET
            DumpArchiveEntry.TYPE.LINK -> PosixFileType.SYMBOLIC_LINK
            DumpArchiveEntry.TYPE.FILE -> PosixFileType.REGULAR_FILE
            DumpArchiveEntry.TYPE.BLKDEV -> PosixFileType.BLOCK_DEVICE
            DumpArchiveEntry.TYPE.DIRECTORY -> PosixFileType.DIRECTORY
            DumpArchiveEntry.TYPE.CHRDEV -> PosixFileType.CHARACTER_DEVICE
            DumpArchiveEntry.TYPE.FIFO -> PosixFileType.FIFO
            DumpArchiveEntry.TYPE.WHITEOUT, DumpArchiveEntry.TYPE.UNKNOWN -> PosixFileType.UNKNOWN
            else -> PosixFileType.UNKNOWN
        }

private val TarArchiveEntry.posixFileType: PosixFileType
    get() =
        when {
            isDirectory -> PosixFileType.DIRECTORY
            isFile -> PosixFileType.REGULAR_FILE
            isSymbolicLink -> PosixFileType.SYMBOLIC_LINK
            isCharacterDevice -> PosixFileType.CHARACTER_DEVICE
            isBlockDevice -> PosixFileType.BLOCK_DEVICE
            isFIFO -> PosixFileType.FIFO
            else -> PosixFileType.UNKNOWN
        }

private val ZipArchiveEntry.posixFileType: PosixFileType
    get() =
        when {
            isDirectory -> PosixFileType.DIRECTORY
            isUnixSymlink -> PosixFileType.SYMBOLIC_LINK
            else -> PosixFileType.REGULAR_FILE
        }

val BasicFileAttributes.posixFileType: PosixFileType
    get() =
        when (this) {
            is PosixFileAttributes -> type()
            else ->
                when {
                    isRegularFile -> PosixFileType.REGULAR_FILE
                    isDirectory -> PosixFileType.DIRECTORY
                    isSymbolicLink -> PosixFileType.SYMBOLIC_LINK
                    else -> PosixFileType.UNKNOWN
                }
        }
