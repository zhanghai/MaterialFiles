/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.Path

class PollingWatchKey(
    watchService: PollingWatchService,
    path: Path
) : AbstractWatchKey<PollingWatchKey, Path>(watchService, path)
