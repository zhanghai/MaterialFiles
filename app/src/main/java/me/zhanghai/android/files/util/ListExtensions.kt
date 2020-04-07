/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

fun <T> List<T>.startsWith(prefix: List<T>): Boolean =
    size >= prefix.size && subList(0, prefix.size) == prefix

fun <T> List<T>.endsWith(suffix: List<T>): Boolean =
    size >= suffix.size && subList(size - suffix.size, size) == suffix
