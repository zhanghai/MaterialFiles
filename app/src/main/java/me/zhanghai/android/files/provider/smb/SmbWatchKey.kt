/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb

import me.zhanghai.android.files.provider.common.AbstractWatchKey

internal class SmbWatchKey(
    watchService: SmbWatchService,
    path: SmbPath
) : AbstractWatchKey<SmbWatchKey, SmbPath>(watchService, path)
