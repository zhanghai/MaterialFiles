/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.theme.typeface

import android.app.Activity
import me.zhanghai.android.files.compat.factory2Compat

object TypefaceHelper {
    private const val FAMILY_NAME_GOOGLE_SANS = "google-sans"

    private val googleSansLayoutInflaterFactory =
        TypefaceLayoutInflaterFactory(FAMILY_NAME_GOOGLE_SANS)

    fun apply(activity: Activity) {
        activity.layoutInflater.factory2Compat = googleSansLayoutInflaterFactory
    }
}
