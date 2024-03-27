package me.zhanghai.kotlin.filesystem.posix

import me.zhanghai.kotlin.filesystem.CreateFileOption

public data class PosixModeOption(public val mode: Set<PosixModeBit>) : CreateFileOption
