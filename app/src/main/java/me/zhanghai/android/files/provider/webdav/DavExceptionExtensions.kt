package me.zhanghai.android.files.provider.webdav

import at.bitfire.dav4jvm.exception.ConflictException
import at.bitfire.dav4jvm.exception.DavException
import at.bitfire.dav4jvm.exception.ForbiddenException
import at.bitfire.dav4jvm.exception.NotFoundException
import at.bitfire.dav4jvm.exception.UnauthorizedException
import java8.nio.file.AccessDeniedException
import java8.nio.file.FileAlreadyExistsException
import java8.nio.file.FileSystemException
import java8.nio.file.NoSuchFileException
import me.zhanghai.android.files.provider.webdav.client.DavIOException

fun DavException.toFileSystemException(
    file: String?,
    other: String? = null
): FileSystemException {
    return when (this) {
        is DavIOException ->
            return FileSystemException(file, other, message).apply { initCause(cause) }
        is UnauthorizedException, is ForbiddenException ->
            AccessDeniedException(file, other, message)
        is NotFoundException -> NoSuchFileException(file, other, message)
        is ConflictException -> FileAlreadyExistsException(file, other, message)
        else -> FileSystemException(file, other, message)
    }.apply { initCause(this@toFileSystemException) }
}
