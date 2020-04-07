/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation

import android.content.Context
import android.content.res.ColorStateList
import me.zhanghai.android.files.R
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.getColorByAttr
import me.zhanghai.android.files.util.isLightTheme
import me.zhanghai.android.files.util.valueCompat

// We cannot reference disabled text color in XML resource, so we have to do this in Java.
object NavigationItemColor {
    private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
    private val DISABLED_STATE_SET = intArrayOf(-android.R.attr.state_enabled)
    private val EMPTY_STATE_SET = intArrayOf()

    fun create(color: ColorStateList, context: Context): ColorStateList {
        // The primary color doesn't have enough contrast against the window background color in a
        // dark theme.
        // But MD2 theme has.
        val checkedColorAttr = if (Settings.MATERIAL_DESIGN_2.valueCompat || context.isLightTheme) {
            R.attr.colorPrimary
        } else {
            android.R.attr.textColorPrimary
        }
        val checkedColor = context.getColorByAttr(checkedColorAttr)
        val defaultColor = color.defaultColor
        val disabledColor = color.getColorForState(DISABLED_STATE_SET, defaultColor)
        return ColorStateList(
            arrayOf(DISABLED_STATE_SET, CHECKED_STATE_SET, EMPTY_STATE_SET),
            intArrayOf(disabledColor, checkedColor, defaultColor)
        )
    }
}
