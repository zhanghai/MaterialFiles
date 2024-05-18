/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.kotlin.files.ui.content

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    content()
}
