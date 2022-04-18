/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.app

import androidx.core.content.edit
import me.zhanghai.android.files.BuildConfig

private const val KEY_VERSION_CODE = "key_version_code"

private const val VERSION_CODE_BELOW_1_1_0 = 17
private const val VERSION_CODE_1_1_0 = 18
private const val VERSION_CODE_1_2_0 = 22
private const val VERSION_CODE_1_3_0 = 24
private const val VERSION_CODE_1_4_0 = 26
private const val VERSION_CODE_1_5_0 = 29
private const val VERSION_CODE_LATEST = BuildConfig.VERSION_CODE

private var lastVersionCode: Int
    get() {
        if (defaultSharedPreferences.all.isEmpty()) {
            // This is a new install.
            lastVersionCode = VERSION_CODE_LATEST
            return VERSION_CODE_LATEST
        }
        return defaultSharedPreferences.getInt(KEY_VERSION_CODE, VERSION_CODE_BELOW_1_1_0)
    }
    set(value) {
        defaultSharedPreferences.edit { putInt(KEY_VERSION_CODE, value) }
    }

fun upgradeApp() {
    upgradeAppFrom(lastVersionCode)
    lastVersionCode = VERSION_CODE_LATEST
}

private fun upgradeAppFrom(lastVersionCode: Int) {
    if (lastVersionCode < VERSION_CODE_1_1_0) {
        upgradeAppTo1_1_0()
    }
    if (lastVersionCode < VERSION_CODE_1_2_0) {
        upgradeAppTo1_2_0()
    }
    if (lastVersionCode < VERSION_CODE_1_3_0) {
        upgradeAppTo1_3_0()
    }
    if (lastVersionCode < VERSION_CODE_1_4_0) {
        upgradeAppTo1_4_0()
    }
    if (lastVersionCode < VERSION_CODE_1_5_0) {
        upgradeAppTo1_5_0()
    }
    // Continue with new `if`s on lastVersionCode instead of `else if`.
}
