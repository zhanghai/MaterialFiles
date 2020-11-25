/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation

import android.content.Context
import android.content.res.ColorStateList
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.getColorByAttr

// We cannot reference disabled text color in XML resource, so we have to do this in Java.
object NavigationItemColor {
    private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
    private val DISABLED_STATE_SET = intArrayOf(-android.R.attr.state_enabled)
    private val EMPTY_STATE_SET = intArrayOf()

    fun create(color: ColorStateList, context: Context): ColorStateList {
        val checkedColor = context.getColorByAttr(R.attr.colorPrimary)
        val defaultColor = color.defaultColor
        val disabledColor = color.getColorForState(DISABLED_STATE_SET, defaultColor)
        return ColorStateList(
            arrayOf(DISABLED_STATE_SET, CHECKED_STATE_SET, EMPTY_STATE_SET),
            intArrayOf(disabledColor, checkedColor, defaultColor)
        )
    }
}
