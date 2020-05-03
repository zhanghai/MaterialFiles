/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

sealed class Stateful<T>(open val value: T?)

class Loading<T>(value: T?) : Stateful<T>(value)

class Failure<T>(value: T?, val throwable: Throwable) : Stateful<T>(value)

class Success<T>(value: T) : Stateful<T>(value) {
    override val value: T
        get() = super.value!!
}
