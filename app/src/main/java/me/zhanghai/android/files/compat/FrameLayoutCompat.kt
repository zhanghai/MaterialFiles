/*
 * Copyright (c) 2025 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.widget.FrameLayout

var FrameLayout.foregroundCompat: Drawable?
    // The get/setForeground() methods were on FrameLayout and are now on View, so this is fine
    // because both are classes and invoke-virtual works for both.
    @SuppressLint("NewApi")
    get() = foreground
    @SuppressLint("NewApi")
    set(value) {
        foreground = value
    }
