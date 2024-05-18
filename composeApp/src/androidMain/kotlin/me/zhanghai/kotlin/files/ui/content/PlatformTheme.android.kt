/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.kotlin.files.ui.content

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun PlatformTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    if (!LocalInspectionMode.current) {
        val context = LocalContext.current
        val view = LocalView.current
        LaunchedEffect(context, view) {
            val window = (context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val windowIsTranslucent =
                context.obtainStyledAttributes(intArrayOf(android.R.attr.windowIsTranslucent)).use {
                    it.getBoolean(0, false)
                }
            val insetsController = WindowCompat.getInsetsController(window, view)
            val lightSystemBars = !(windowIsTranslucent || darkTheme)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.statusBarColor = Color.Transparent.toArgb()
                insetsController.isAppearanceLightStatusBars = lightSystemBars
            }
            // android:windowLightNavigationBar is API 27 despite that
            // View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR is API 26.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                window.navigationBarColor = Color.Transparent.toArgb()
                insetsController.isAppearanceLightNavigationBars = lightSystemBars
            }
        }
    }
    content()
}
