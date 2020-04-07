/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.icu.text.ListFormatter
import android.os.Build

object ListFormatterCompat {
    fun format(vararg items: Any?): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ListFormatter.getInstance().format(*items)
        } else {
            formatCompat(items.asList())
        }

    fun format(items: Collection<*>): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ListFormatter.getInstance().format(items)
        } else {
            formatCompat(items)
        }

    private fun formatCompat(items: Collection<*>): String = items.joinToString(", ")
}
