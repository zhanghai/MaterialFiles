package me.zhanghai.kotlin.filesystem

public interface FileMetadata {
    public val id: Any

    public val type: FileType

    public val size: Long

    public val lastModificationTimeMillis: Long

    public val lastAccessTimeMillis: Long?

    public val creationTimeMillis: Long?
}
