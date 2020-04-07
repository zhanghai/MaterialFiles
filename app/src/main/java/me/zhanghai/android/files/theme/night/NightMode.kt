/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.theme.night

import androidx.appcompat.app.AppCompatDelegate

enum class NightMode(val value: Int) {
    FOLLOW_SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    OFF(AppCompatDelegate.MODE_NIGHT_NO),
    ON(AppCompatDelegate.MODE_NIGHT_YES),
    AUTO_TIME(AppCompatDelegate.MODE_NIGHT_AUTO_TIME),
    AUTO_BATTERY(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
}
