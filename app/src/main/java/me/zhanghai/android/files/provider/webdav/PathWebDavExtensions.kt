/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.webdav

import java8.nio.file.Path
import me.zhanghai.android.files.provider.webdav.client.Authority

fun Authority.createWebDavRootPath(): Path =
    WebDavFileSystemProvider.getOrNewFileSystem(this).rootDirectory
