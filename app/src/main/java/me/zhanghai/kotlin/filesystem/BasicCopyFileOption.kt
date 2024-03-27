package me.zhanghai.kotlin.filesystem

public enum class BasicCopyFileOption : CopyFileOption {
    REPLACE_EXISTING,
    COPY_METADATA,
    ATOMIC_MOVE
}
