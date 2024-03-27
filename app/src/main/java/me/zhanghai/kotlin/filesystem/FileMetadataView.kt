package me.zhanghai.kotlin.filesystem

import me.zhanghai.kotlin.filesystem.io.AsyncCloseable

public interface FileMetadataView : AsyncCloseable {
    public suspend fun readMetadata(): FileMetadata

    public suspend fun setTimes(
        lastModificationTimeMillis: Long? = null,
        lastAccessTimeMillis: Long? = null,
        creationTimeMillis: Long? = null
    )
}
