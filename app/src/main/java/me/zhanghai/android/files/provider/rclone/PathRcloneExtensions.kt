/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.rclone

import java8.nio.file.Path

fun RcloneAuthority.createRcloneRootPath(): Path =
    RcloneFileSystemProvider.getOrNewFileSystem(this).rootDirectory

val Path.isRclonePath: Boolean
    get() = this is RclonePath
