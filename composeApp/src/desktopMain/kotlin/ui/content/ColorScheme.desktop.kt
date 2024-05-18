/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.kotlin.files.ui.content

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

// TODO: Consider https://github.com/jordond/MaterialKolor
@Composable
actual fun dynamicColorScheme(darkTheme: Boolean): ColorScheme =
    if (darkTheme) darkColorScheme() else lightColorScheme()
