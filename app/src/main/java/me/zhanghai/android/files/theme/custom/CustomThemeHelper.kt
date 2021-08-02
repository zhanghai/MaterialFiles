/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.theme.custom

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import me.zhanghai.android.files.compat.recreateCompat
import me.zhanghai.android.files.compat.setThemeCompat
import me.zhanghai.android.files.compat.themeResIdCompat
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.theme.night.NightModeHelper
import me.zhanghai.android.files.util.SimpleActivityLifecycleCallbacks
import me.zhanghai.android.files.util.valueCompat

object CustomThemeHelper {
    private val activityBaseThemes = mutableMapOf<Activity, Int>()

    fun initialize(application: Application) {
        application.registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                check(activityBaseThemes.containsKey(activity)) {
                    "Activity must extend AppActivity: $activity"
                }
            }

            override fun onActivityDestroyed(activity: Activity) {
                activityBaseThemes.remove(activity)
            }
        })
    }

    fun apply(activity: Activity) {
        val baseThemeRes = activity.themeResIdCompat
        activityBaseThemes[activity] = baseThemeRes
        val customThemeRes = getCustomThemeRes(baseThemeRes, activity)
        activity.setThemeCompat(customThemeRes)
    }

    fun sync() {
        for ((activity, baseThemeRes) in activityBaseThemes) {
            val currentThemeRes = activity.themeResIdCompat
            val customThemeRes = getCustomThemeRes(baseThemeRes, activity)
            if (currentThemeRes != customThemeRes) {
                // Ignore ".Black" theme changes when not in night mode.
                if (!NightModeHelper.isInNightMode(activity as AppCompatActivity)
                    && isBlackThemeChange(currentThemeRes, customThemeRes, activity)) {
                    continue
                }
                if (activity is OnThemeChangedListener) {
                    (activity as OnThemeChangedListener).onThemeChanged(customThemeRes)
                } else {
                    activity.recreateCompat()
                }
            }
        }
    }

    private fun getCustomThemeRes(@StyleRes baseThemeRes: Int, context: Context): Int {
        val resources = context.resources
        val baseThemeName = resources.getResourceName(baseThemeRes)
        val customThemeIdentifier = if (Settings.MATERIAL_DESIGN_3.valueCompat) {
            "Md3"
        } else {
            resources.getResourceEntryName(Settings.THEME_COLOR.valueCompat.resourceId)
        }
        val blackThemeSuffix = if (Settings.BLACK_NIGHT_MODE.valueCompat) ".Black" else ""
        val customThemeName = "$baseThemeName.$customThemeIdentifier$blackThemeSuffix"
        return resources.getIdentifier(customThemeName, null, null)
    }

    private fun isBlackThemeChange(
        @StyleRes themeRes1: Int,
        @StyleRes themeRes2: Int,
        context: Context
    ): Boolean {
        val resources = context.resources
        val themeName1 = resources.getResourceName(themeRes1)
        val themeName2 = resources.getResourceName(themeRes2)
        return themeName1 == "$themeName2.Black" || themeName2 == "$themeName1.Black"
    }

    interface OnThemeChangedListener {
        fun onThemeChanged(@StyleRes theme: Int)
    }
}
