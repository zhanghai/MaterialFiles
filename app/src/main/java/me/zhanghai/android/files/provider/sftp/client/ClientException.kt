/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.sftp.client

import java8.nio.file.AccessDeniedException
import java8.nio.file.DirectoryNotEmptyException
import java8.nio.file.FileAlreadyExistsException
import java8.nio.file.FileSystemException
import java8.nio.file.FileSystemLoopException
import java8.nio.file.NoSuchFileException
import java8.nio.file.NotDirectoryException
import me.zhanghai.android.files.provider.common.InvalidFileNameException
import me.zhanghai.android.files.provider.common.IsDirectoryException
import me.zhanghai.android.files.provider.common.ReadOnlyFileSystemException
import net.schmizz.sshj.sftp.Response
import net.schmizz.sshj.sftp.SFTPException

class ClientException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(cause: Throwable?) : super(cause)

    private val statusCode: Response.StatusCode? = (cause as? SFTPException)?.statusCode

    fun toFileSystemException(file: String?, other: String? = null): FileSystemException =
        when (statusCode) {
            Response.StatusCode.NO_SUCH_FILE, Response.StatusCode.NO_SUCH_PATH,
            Response.StatusCode.DELETE_PENDING -> NoSuchFileException(file, other, message)
            Response.StatusCode.PERMISSION_DENIED, Response.StatusCode.CANNOT_DELETE ->
                AccessDeniedException(file, other, message)
            Response.StatusCode.FILE_ALREADY_EXISTS ->
                FileAlreadyExistsException(file, other, message)
            Response.StatusCode.WRITE_PROTECT -> ReadOnlyFileSystemException(file, other, message)
            Response.StatusCode.DIR_NOT_EMPTY -> DirectoryNotEmptyException(file)
            Response.StatusCode.NOT_A_DIRECTORY -> NotDirectoryException(file)
            Response.StatusCode.INVALID_FILENAME -> InvalidFileNameException(file, other, message)
            Response.StatusCode.LINK_LOOP -> FileSystemLoopException(file)
            Response.StatusCode.FILE_IS_A_DIRECTORY -> IsDirectoryException(file, other, message)
            else -> FileSystemException(file, other, message)
        }.apply { initCause(this@ClientException) }
}
