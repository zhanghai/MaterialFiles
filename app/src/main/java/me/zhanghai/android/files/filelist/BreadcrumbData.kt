/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import android.content.Context
import java8.nio.file.Path

data class BreadcrumbData(
    val paths: List<Path>,
    val nameProducers: List<(Context) -> String>,
    val selectedIndex: Int
)
