package me.zhanghai.kotlin.filesystem

public enum class BasicFileContentOption : FileContentOption {
    READ,
    WRITE,
    APPEND,
    TRUNCATE_EXISTING,
    CREATE,
    CREATE_NEW
}
