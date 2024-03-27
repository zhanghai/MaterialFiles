package me.zhanghai.kotlin.filesystem

public interface FileSystemProvider {
    public val scheme: String

    public fun createFileSystem(rootUri: Uri): FileSystem
}
