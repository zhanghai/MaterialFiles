/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.DirectoryStream
import java8.nio.file.Path

class PathListDirectoryStream(
    paths: List<Path>,
    filter: DirectoryStream.Filter<in Path>
) : PathIteratorDirectoryStream(paths.iterator(), null, filter)
