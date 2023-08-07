/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.os.Build
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.core.widget.TextViewCompat
import me.zhanghai.android.files.util.lazyReflectedMethod

private val isSingleLineMethod by lazyReflectedMethod(TextView::class.java, "isSingleLine")

val TextView.isSingleLineCompat: Boolean
    get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isSingleLine
        } else {
            isSingleLineMethod.invoke(this) as Boolean
        }

fun TextView.setTextAppearanceCompat(@StyleRes resId: Int) {
    TextViewCompat.setTextAppearance(this, resId)
}
