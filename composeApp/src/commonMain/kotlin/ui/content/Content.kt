/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.kotlin.files.ui.content

import androidx.compose.runtime.Composable

@Composable
fun Content(content: @Composable () -> Unit) {
    AppTheme(content = content)
}
