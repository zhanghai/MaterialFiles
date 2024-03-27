package me.zhanghai.kotlin.filesystem

import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

public interface FileSystemProvider {
    public val scheme: String

    @Throws(CancellationException::class, IOException::class)
    public suspend fun getFileSystem(path: Path): FileSystem
}
