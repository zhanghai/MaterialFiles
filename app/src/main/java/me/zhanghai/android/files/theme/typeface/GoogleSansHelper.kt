/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.theme.typeface

object GoogleSansHelper {
    private const val FAMILY_NAME_GOOGLE_SANS = "google-sans"

    val isAvailable: Boolean
        get() = isActive || !TypefaceHelper.isDefaultTypeface(FAMILY_NAME_GOOGLE_SANS)

    var isActive: Boolean = false
        private set

    fun initialize() {
        if (!isAvailable) {
            return
        }
        try {
            TypefaceHelper.replaceDefaultAndSansSerifTypefaces(FAMILY_NAME_GOOGLE_SANS)
            isActive = true
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}
