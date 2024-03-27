package me.zhanghai.kotlin.filesystem.posix

import me.zhanghai.kotlin.filesystem.FileMetadata
import me.zhanghai.kotlin.filesystem.FileType

public interface PosixFileMetadata : FileMetadata {
    public val posixType: PosixFileType

    override val type: FileType
        get() =
            when (posixType) {
                PosixFileType.DIRECTORY -> FileType.DIRECTORY
                PosixFileType.REGULAR_FILE -> FileType.REGULAR_FILE
                PosixFileType.SYMBOLIC_LINK -> FileType.SYMBOLIC_LINK
                else -> FileType.OTHER
            }

    public val mode: Set<PosixModeBit>

    public val userId: Int

    public val groupId: Int
}
