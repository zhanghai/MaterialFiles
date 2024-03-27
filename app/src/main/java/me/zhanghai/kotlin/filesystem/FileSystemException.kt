package me.zhanghai.kotlin.filesystem

import kotlinx.io.IOException

public open class FileSystemException(
    public val file: Path?,
    public val otherFile: Path? = null,
    message: String? = null,
    cause: Throwable? = null
) : IOException(message, cause)
