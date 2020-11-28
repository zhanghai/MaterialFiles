/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.graphics.Typeface
import me.zhanghai.java.reflected.ReflectedField

private val nativeInstanceField = ReflectedField(Typeface::class.java, "native_instance")

val Typeface.nativeInstanceCompat: Long
    get() = nativeInstanceField.getLong(this)
