package me.zhanghai.kotlin.filesystem

import kotlinx.io.bytestring.ByteString

public interface DirectoryEntry {
    public val name: ByteString

    public val type: FileType?

    public val metadata: FileMetadata?
}
