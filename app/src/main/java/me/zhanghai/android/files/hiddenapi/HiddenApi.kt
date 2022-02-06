/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.hiddenapi

import android.os.Build

object HiddenApi {
    fun disableHiddenApiChecks() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            System.loadLibrary("hiddenapi")
        }
    }
}
