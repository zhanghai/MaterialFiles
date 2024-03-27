package me.zhanghai.kotlin.filesystem.posix

import me.zhanghai.kotlin.filesystem.FileMetadataView

public interface PosixFileMetadataView : FileMetadataView {
    override suspend fun readMetadata(): PosixFileMetadata

    public suspend fun setMode(mode: Set<PosixModeBit>)

    public suspend fun setOwnership(userId: Int? = null, groupId: Int? = null)
}
