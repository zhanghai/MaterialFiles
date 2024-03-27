package me.zhanghai.kotlin.filesystem

import me.zhanghai.kotlin.filesystem.io.AsyncCloseable

public interface FileStore : AsyncCloseable {
    public suspend fun readMetadata(): FileStoreMetadata
}
