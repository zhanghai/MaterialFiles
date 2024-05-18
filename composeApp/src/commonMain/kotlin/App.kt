/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.kotlin.files

import androidx.compose.runtime.Composable
import me.zhanghai.kotlin.files.ui.content.Content
import me.zhanghai.kotlin.files.ui.filelist.FileListScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    Content { FileListScreen() }
}
