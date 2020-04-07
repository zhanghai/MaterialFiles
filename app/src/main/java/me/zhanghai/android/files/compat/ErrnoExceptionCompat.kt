/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.system.ErrnoException
import me.zhanghai.java.reflected.ReflectedField

@RestrictedHiddenApi
private val functionNameField = ReflectedField(ErrnoException::class.java, "functionName")

val ErrnoException.functionNameCompat: String
    get() = functionNameField.getObject(this)
