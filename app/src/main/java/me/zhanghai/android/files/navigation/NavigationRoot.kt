/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation

import android.content.Context
import java8.nio.file.Path

interface NavigationRoot {
    val path: Path

    fun getName(context: Context): String
}
