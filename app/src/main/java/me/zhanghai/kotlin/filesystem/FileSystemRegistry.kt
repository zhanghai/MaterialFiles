package me.zhanghai.kotlin.filesystem

import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

public object FileSystemRegistry {
    public val providers: Map<String, FileSystemProvider> = mutableMapOf()

    @Throws(CancellationException::class, IOException::class)
    public suspend inline fun getFileSystem(path: Path): FileSystem {
        val provider = providers[path.scheme]
        requireNotNull(provider) { "No file system provider for scheme \"${path.scheme}\"" }
        return provider.getFileSystem(path)
    }
}
