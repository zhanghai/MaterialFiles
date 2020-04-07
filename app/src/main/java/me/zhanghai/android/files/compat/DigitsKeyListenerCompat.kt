/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.os.Build
import android.text.method.DigitsKeyListener
import java.util.Locale

object DigitsKeyListenerCompat {
    fun getInstance(locale: Locale?, sign: Boolean, decimal: Boolean): DigitsKeyListener =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DigitsKeyListener.getInstance(locale, sign, decimal)
        } else {
            @Suppress("DEPRECATION")
            DigitsKeyListener.getInstance(sign, decimal)
        }
}
