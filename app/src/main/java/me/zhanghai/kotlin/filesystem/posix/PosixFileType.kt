package me.zhanghai.kotlin.filesystem.posix

public enum class PosixFileType {
    DIRECTORY,
    CHARACTER_DEVICE,
    BLOCK_DEVICE,
    REGULAR_FILE,
    FIFO,
    SYMBOLIC_LINK,
    SOCKET,
    OTHER
}
