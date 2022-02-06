/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.hiddenapi

import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.P)
object HiddenApi {
    init {
        System.loadLibrary("hiddenapi")
    }

    external fun setExemptions(signaturePrefixes: Array<String>): Boolean
}
