/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.app.ActivityManager.TaskDescription
import androidx.annotation.ColorInt
import me.zhanghai.java.reflected.ReflectedMethod

@RestrictedHiddenApi
private val setPrimaryColorMethod =
    ReflectedMethod(TaskDescription::class.java, "setPrimaryColor", Int::class.java)

fun TaskDescription.setPrimaryColorCompat(@ColorInt primaryColor: Int) {
    setPrimaryColorMethod.invoke<Unit>(this, primaryColor)
}
