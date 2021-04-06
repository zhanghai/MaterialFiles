/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall

import android.system.ErrnoException
import android.system.OsConstants
import java8.nio.file.AccessDeniedException
import java8.nio.file.AtomicMoveNotSupportedException
import java8.nio.file.DirectoryNotEmptyException
import java8.nio.file.FileAlreadyExistsException
import java8.nio.file.FileSystemException
import java8.nio.file.FileSystemLoopException
import java8.nio.file.NoSuchFileException
import java8.nio.file.NotDirectoryException
import java8.nio.file.NotLinkException
import me.zhanghai.android.files.compat.functionNameCompat
import me.zhanghai.android.files.provider.common.InvalidFileNameException
import me.zhanghai.android.files.provider.common.IsDirectoryException
import me.zhanghai.android.files.provider.common.ReadOnlyFileSystemException

class SyscallException @JvmOverloads constructor(
    val functionName: String,
    val errno: Int,
    cause: Throwable? = null
) : Exception(perror(errno, functionName), cause) {

    constructor(errnoException: ErrnoException) : this(
        errnoException.functionNameCompat, errnoException.errno, errnoException
    )

    @Throws(AtomicMoveNotSupportedException::class)
    fun maybeThrowAtomicMoveNotSupportedException(file: String?, other: String?) {
        if (errno == OsConstants.EXDEV) {
            throw AtomicMoveNotSupportedException(file, other, message)
                .apply { initCause(this@SyscallException) }
        }
    }

    @Throws(InvalidFileNameException::class)
    fun maybeThrowInvalidFileNameException(file: String?) {
        if (errno == OsConstants.EINVAL) {
            throw InvalidFileNameException(file, null, message)
                .apply { initCause(this@SyscallException) }
        }
    }

    @Throws(NotLinkException::class)
    fun maybeThrowNotLinkException(file: String?) {
        if (errno == OsConstants.EINVAL) {
            throw InvalidFileNameException(file, null, message)
                .apply { initCause(this@SyscallException) }
        }
    }

    fun toFileSystemException(file: String?, other: String? = null): FileSystemException =
        when (errno) {
            OsConstants.EACCES, OsConstants.EPERM -> AccessDeniedException(file, other, message)
            OsConstants.EEXIST -> FileAlreadyExistsException(file, other, message)
            OsConstants.EISDIR -> IsDirectoryException(file, other, message)
            OsConstants.ELOOP -> FileSystemLoopException(file)
            OsConstants.ENOTDIR -> NotDirectoryException(file)
            OsConstants.ENOTEMPTY -> DirectoryNotEmptyException(file)
            OsConstants.ENOENT -> NoSuchFileException(file, other, message)
            OsConstants.EROFS -> ReadOnlyFileSystemException(file, other, message)
            else -> FileSystemException(file, other, message)
        }.apply { initCause(this@SyscallException) }

    companion object {
        private fun perror(errno: Int, functionName: String): String =
            "$functionName: ${Syscalls.strerror(errno)}"
    }
}
