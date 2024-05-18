/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.kotlin.files.ui.content

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme =
        when {
            dynamicColor -> dynamicColorScheme(darkTheme)
            darkTheme -> darkColorScheme()
            else -> lightColorScheme()
        }
    MaterialTheme(colorScheme = colorScheme) {
        PlatformTheme(darkTheme = darkTheme, content = content)
    }
}
