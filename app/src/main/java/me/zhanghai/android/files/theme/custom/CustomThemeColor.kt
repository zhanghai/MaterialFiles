/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.theme.custom

import androidx.annotation.ColorRes

interface CustomThemeColor {
    @get:ColorRes
    val resourceId: Int
    val resourceEntryName: String
}
