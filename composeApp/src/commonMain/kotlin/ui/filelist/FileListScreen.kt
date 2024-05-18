/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.kotlin.files.ui.filelist

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FileListScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { FileListTopBar() },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { contentPadding ->
    }
}
