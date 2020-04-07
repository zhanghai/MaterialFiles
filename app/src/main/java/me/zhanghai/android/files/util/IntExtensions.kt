/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

fun Int.hasBits(bits: Int): Boolean = this and bits == bits

infix fun Int.andInv(other: Int): Int = this and other.inv()
