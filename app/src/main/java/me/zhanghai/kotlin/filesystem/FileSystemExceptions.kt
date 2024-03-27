package me.zhanghai.kotlin.filesystem

public class AccessDeniedException(
    file: Path?,
    otherFile: Path? = null,
    message: String? = null,
    cause: Throwable? = null
) : FileSystemException(file, otherFile, message, cause)

public class AtomicMoveNotSupportedException(
    file: Path?,
    otherFile: Path? = null,
    message: String? = null,
    cause: Throwable? = null
) : FileSystemException(file, otherFile, message, cause)

public class DirectoryNotEmptyException(
    file: Path?,
    otherFile: Path? = null,
    message: String? = null,
    cause: Throwable? = null
) : FileSystemException(file, otherFile, message, cause)

public class FileAlreadyExistsException(
    file: Path?,
    otherFile: Path? = null,
    message: String? = null,
    cause: Throwable? = null
) : FileSystemException(file, otherFile, message, cause)

public class FileSystemLoopException(
    file: Path?,
    otherFile: Path? = null,
    message: String? = null,
    cause: Throwable? = null
) : FileSystemException(file, otherFile, message, cause)

public class NoSuchFileException(
    file: Path?,
    otherFile: Path? = null,
    message: String? = null,
    cause: Throwable? = null
) : FileSystemException(file, otherFile, message, cause)

public class NotDirectoryException(
    file: Path?,
    otherFile: Path? = null,
    message: String? = null,
    cause: Throwable? = null
) : FileSystemException(file, otherFile, message, cause)

public class NotLinkException(
    file: Path?,
    otherFile: Path? = null,
    message: String? = null,
    cause: Throwable? = null
) : FileSystemException(file, otherFile, message, cause)
