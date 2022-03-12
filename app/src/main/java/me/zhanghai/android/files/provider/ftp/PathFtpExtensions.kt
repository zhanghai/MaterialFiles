/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.ftp

import java8.nio.file.Path
import me.zhanghai.android.files.provider.ftp.client.Authority

fun Authority.createFtpRootPath(): Path =
    FtpFileSystemProvider.getOrNewFileSystem(this).rootDirectory
