/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.content.pm.ApplicationInfo
import android.os.Build
import me.zhanghai.java.reflected.ReflectedField

@RestrictedHiddenApi
private val versionCodeField = ReflectedField(ApplicationInfo::class.java, "versionCode")

@RestrictedHiddenApi
private val longVersionCodeField = ReflectedField(ApplicationInfo::class.java, "longVersionCode")

val ApplicationInfo.longVersionCodeCompat: Long
    get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            longVersionCodeField.getLong(this)
        } else {
            versionCodeField.getInt(this).toLong()
        }
