package me.zhanghai.android.files.provider.ftp.client

import java8.nio.file.AccessDeniedException
import java8.nio.file.FileSystemException
import java8.nio.file.NoSuchFileException
import me.zhanghai.android.files.provider.common.InvalidFileNameException
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.IOException

class NegativeReplyCodeException(
    private val replyCode: Int,
    replyString: String
) : IOException(replyString) {
    fun toFileSystemException(file: String?, other: String? = null): FileSystemException =
        when (replyCode) {
            FTPReply.NOT_LOGGED_IN, FTPReply.NEED_ACCOUNT_FOR_STORING_FILES ->
                AccessDeniedException(file, other, message)
            FTPReply.FILE_UNAVAILABLE -> NoSuchFileException(file, other, message)
            FTPReply.FILE_NAME_NOT_ALLOWED -> InvalidFileNameException(file, other, message)
            else -> FileSystemException(file, other, message)
        }.apply { initCause(this@NegativeReplyCodeException) }
}

internal fun FTPClient.createNegativeReplyCodeException() =
    NegativeReplyCodeException(replyCode, replyString)

internal fun FTPClient.throwNegativeReplyCodeException(): Nothing {
    throw createNegativeReplyCodeException()
}
