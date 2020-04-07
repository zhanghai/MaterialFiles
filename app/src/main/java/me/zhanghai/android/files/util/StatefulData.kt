/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

open class StatefulData<T> protected constructor(
    val state: State,
    val data: T?,
    val exception: Exception?
) {
    enum class State {
        LOADING,
        ERROR,
        SUCCESS
    }
}
