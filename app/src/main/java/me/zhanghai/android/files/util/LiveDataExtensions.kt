/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map

@Suppress("UNCHECKED_CAST")
val <T> LiveData<T>.valueCompat: T
    get() = value as T

inline fun <X, Y> LiveData<X>.mapDistinct(crossinline mapFunction: (X) -> Y): LiveData<Y> =
    map(mapFunction).distinctUntilChanged()
