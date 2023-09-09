/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import android.system.OsConstants
import java8.nio.file.attribute.BasicFileAttributes

// https://www.gnu.org/software/libc/manual/html_node/Testing-File-Type.html
enum class PosixFileType(val mode: Int) {
    UNKNOWN(0),
    DIRECTORY(OsConstants.S_IFDIR),
    CHARACTER_DEVICE(OsConstants.S_IFCHR),
    BLOCK_DEVICE(OsConstants.S_IFBLK),
    REGULAR_FILE(OsConstants.S_IFREG),
    FIFO(OsConstants.S_IFIFO),
    SYMBOLIC_LINK(OsConstants.S_IFLNK),
    SOCKET((OsConstants.S_IFSOCK));

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
