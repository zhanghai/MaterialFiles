/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file

import java.io.File

object JavaFile {
    fun isDirectory(path: String): Boolean = File(path).isDirectory

    fun getFreeSpace(path: String): Long = File(path).freeSpace

    fun getTotalSpace(path: String): Long = File(path).totalSpace
}
