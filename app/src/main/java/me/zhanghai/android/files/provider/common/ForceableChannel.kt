/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java.io.IOException
import java.nio.channels.Channel
import java.nio.channels.FileChannel

interface ForceableChannel {
    @Throws(IOException::class)
    fun force(metaData: Boolean)
}

val Channel.isForceable: Boolean
    get() = this is FileChannel || this is ForceableChannel

@Throws(IOException::class)
fun Channel.force(metaData: Boolean) {
    when (this) {
        is FileChannel -> force(metaData)
        is ForceableChannel -> force(metaData)
        else -> throw UnsupportedOperationException()
    }
}
