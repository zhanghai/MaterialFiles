/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filesystem;

// https://www.gnu.org/software/libc/manual/html_node/Testing-File-Type.html
public enum PosixFileType {
    UNKNOWN,
    DIRECTORY,
    CHARACTER_DEVICE,
    BLOCK_DEVICE,
    REGULAR_FILE,
    FIFO,
    SYMBOLIC_LINK,
    SOCKET
}
