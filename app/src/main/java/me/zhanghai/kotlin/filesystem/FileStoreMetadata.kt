package me.zhanghai.kotlin.filesystem

import kotlinx.io.bytestring.ByteString

public interface FileStoreMetadata {
    public val type: ByteString

    public val blockSize: Long

    public val totalSpace: Long

    public val freeSpace: Long

    public val availableSpace: Long
}
