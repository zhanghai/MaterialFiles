/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import java.io.Closeable

interface PathObservable : Closeable {
    fun addObserver(observer: () -> Unit)

    fun removeObserver(observer: () -> Unit)
}
