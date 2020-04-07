/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.util.linkedHashSetOf
import java.util.LinkedHashSet

// TODO: Make immutable?
class PasteState(
    var copy: Boolean = false,
    val files: LinkedHashSet<FileItem> = linkedHashSetOf()
)
