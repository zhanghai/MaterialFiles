/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.view.LayoutInflater
import androidx.core.view.LayoutInflaterCompat

var LayoutInflater.factory2Compat: LayoutInflater.Factory2
    get() = factory2
    set(value) {
        LayoutInflaterCompat.setFactory2(this, value)
    }
