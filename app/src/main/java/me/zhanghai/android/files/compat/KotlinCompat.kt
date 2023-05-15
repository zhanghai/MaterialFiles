/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import kotlin.comparisons.reversed as kotlinReversed

fun <T> Comparator<T>.reversedCompat(): Comparator<T> = kotlinReversed()
