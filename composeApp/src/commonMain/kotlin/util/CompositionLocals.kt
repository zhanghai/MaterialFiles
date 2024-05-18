/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.kotlin.files.util

fun noLocalProvidedFor(name: String): Nothing = error("CompositionLocal $name not present")
