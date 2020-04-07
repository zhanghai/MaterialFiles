/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import androidx.lifecycle.LiveData
import java.io.Closeable

abstract class CloseableLiveData<T> : LiveData<T>, Closeable {
    constructor(value: T) : super(value)

    constructor()

    abstract override fun close()
}
