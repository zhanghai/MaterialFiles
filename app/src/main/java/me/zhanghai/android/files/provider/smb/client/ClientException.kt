/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb.client

import com.hierynomus.mserref.NtStatus
import com.hierynomus.mssmb2.SMBApiException
import java8.nio.file.AccessDeniedException
import java8.nio.file.AtomicMoveNotSupportedException
import java8.nio.file.DirectoryNotEmptyException
import java8.nio.file.FileAlreadyExistsException
import java8.nio.file.FileSystemException
import java8.nio.file.NoSuchFileException
import java8.nio.file.NotDirectoryException
import java8.nio.file.NotLinkException
import me.zhanghai.android.files.provider.common.InvalidFileNameException
import me.zhanghai.android.files.provider.common.IsDirectoryException

class ClientException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(cause: Throwable?) : super(cause)

    private val status: NtStatus? = (cause as? SMBApiException)?.status
    private val statusCode: Long? = (cause as? SMBApiException)?.statusCode

    @Throws(AtomicMoveNotSupportedException::class)
    fun maybeThrowAtomicMoveNotSupportedException(file: String?, other: String?) {
        if (status == NtStatus.STATUS_NOT_SAME_DEVICE) {
            throw AtomicMoveNotSupportedException(file, other, message)
                .apply { initCause(this@ClientException) }
        }
    }

    @Throws(InvalidFileNameException::class)
    fun maybeThrowInvalidFileNameException(file: String?) {
        if (status == NtStatus.STATUS_OBJECT_NAME_INVALID) {
            throw InvalidFileNameException(file, null, message)
                .apply { initCause(this@ClientException) }
        }
    }

    fun toFileSystemException(file: String?, other: String? = null): FileSystemException =
        when (status) {
            NtStatus.STATUS_ACCESS_DENIED, NtStatus.STATUS_SHARING_VIOLATION,
            NtStatus.STATUS_PRIVILEGE_NOT_HELD, NtStatus.STATUS_LOGON_FAILURE,
            NtStatus.STATUS_PASSWORD_EXPIRED, NtStatus.STATUS_ACCOUNT_DISABLED,
            NtStatus.STATUS_OPLOCK_NOT_GRANTED, NtStatus.STATUS_CANNOT_DELETE,
            NtStatus.STATUS_LOGON_TYPE_NOT_GRANTED, NtStatus.STATUS_USER_SESSION_DELETED,
            NtStatus.STATUS_FILE_ENCRYPTED, NtStatus.STATUS_NETWORK_SESSION_EXPIRED ->
                AccessDeniedException(file, other, message)
            NtStatus.STATUS_OBJECT_NAME_COLLISION ->
                FileAlreadyExistsException(file, other, message)
            NtStatus.STATUS_FILE_IS_A_DIRECTORY -> IsDirectoryException(file, other, message)
            NtStatus.STATUS_NOT_A_DIRECTORY -> NotDirectoryException(file)
            NtStatus.STATUS_DIRECTORY_NOT_EMPTY -> DirectoryNotEmptyException(file)
            NtStatus.STATUS_NO_SUCH_FILE, NtStatus.STATUS_OBJECT_NAME_NOT_FOUND,
            NtStatus.STATUS_OBJECT_PATH_NOT_FOUND, NtStatus.STATUS_DELETE_PENDING,
            NtStatus.STATUS_BAD_NETWORK_PATH, NtStatus.STATUS_NETWORK_NAME_DELETED,
            NtStatus.STATUS_BAD_NETWORK_NAME, NtStatus.STATUS_NOT_FOUND ->
                NoSuchFileException(file, other, message)
            else -> when (statusCode) {
                NtStatuses.STATUS_NOT_A_REPARSE_POINT, NtStatuses.STATUS_IO_REPARSE_TAG_INVALID,
                NtStatuses.STATUS_IO_REPARSE_TAG_MISMATCH,
                NtStatus.STATUS_IO_REPARSE_TAG_NOT_HANDLED.value ->
                    NotLinkException(file, other, message)
                else -> FileSystemException(file, other, message)
            }
        }.apply { initCause(this@ClientException) }
}
