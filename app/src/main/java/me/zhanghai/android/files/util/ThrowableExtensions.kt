/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

val Throwable.hasInterruptedCause: Boolean
    get() {
        var current: Throwable? = this
        do {
            if (current is InterruptedException) {
                return true
            }
            current = current!!.cause
        } while (current != null)
        return false
    }
