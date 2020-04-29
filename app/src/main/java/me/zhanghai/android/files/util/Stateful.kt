/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

sealed class Stateful<out T>

object Loading : Stateful<Nothing>()

class Failure(val throwable: Throwable) : Stateful<Nothing>()

class Success<T>(val value: T) : Stateful<T>()
