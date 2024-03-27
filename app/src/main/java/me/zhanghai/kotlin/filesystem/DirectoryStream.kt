package me.zhanghai.kotlin.filesystem

import me.zhanghai.kotlin.filesystem.io.AsyncCloseable

public interface DirectoryStream : AsyncCloseable {
    public suspend fun read(): DirectoryEntry?
}
