/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.app.Activity
import android.app.ActivityManager.TaskDescription
import android.graphics.Color
import android.os.Build
import androidx.annotation.StyleRes
import androidx.core.app.ActivityCompat
import androidx.core.graphics.ColorUtils
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.getColorByAttr
import me.zhanghai.java.reflected.ReflectedField

@RestrictedHiddenApi
private val taskDescriptionField = ReflectedField(Activity::class.java, "mTaskDescription")

fun Activity.recreateCompat() {
    ActivityCompat.recreate(this)
}

fun Activity.setThemeCompat(@StyleRes resid: Int) {
    setTheme(resid)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val taskDescription = taskDescriptionField.getObject<TaskDescription>(this)
        var appBarSurfaceColor = getColorByAttr(R.attr.colorAppBarSurface)
        if (appBarSurfaceColor == 0 || taskDescription.primaryColor == appBarSurfaceColor) {
            return
        }
        if (Color.alpha(appBarSurfaceColor) != 0xFF) {
            appBarSurfaceColor = ColorUtils.setAlphaComponent(appBarSurfaceColor, 0xFF)
        }
        taskDescription.setPrimaryColorCompat(appBarSurfaceColor)
        setTaskDescription(taskDescription)
    }
}
