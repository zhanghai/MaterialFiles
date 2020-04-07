/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java8.nio.file.Path
import java.io.IOException

interface Searchable {
    @Throws(IOException::class)
    fun search(directory: Path, query: String, intervalMillis: Long, listener: (List<Path>) -> Unit)
}
