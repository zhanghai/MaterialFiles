/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import java8.nio.file.Path
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.util.LinkedMapSet

class FileItemSet : LinkedMapSet<Path, FileItem>(FileItem::path)

fun fileItemSetOf(vararg files: FileItem) = FileItemSet().apply { addAll(files) }
