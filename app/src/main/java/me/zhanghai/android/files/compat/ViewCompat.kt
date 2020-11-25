/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.view.View
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat

@Suppress("UNCHECKED_CAST")
fun <T : View> View.requireViewByIdCompat(@IdRes id: Int) : T =
    ViewCompat.requireViewById(this, id) as T
