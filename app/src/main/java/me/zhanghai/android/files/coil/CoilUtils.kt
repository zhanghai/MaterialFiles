/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.coil

import android.graphics.Bitmap
import android.os.Build
import coil.size.Dimension
import coil.size.Scale
import coil.size.Size
import coil.size.isOriginal
import coil.size.pxOrElse

val Bitmap.Config.isHardware: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this == Bitmap.Config.HARDWARE

fun Bitmap.Config.toSoftware(): Bitmap.Config = if (isHardware) Bitmap.Config.ARGB_8888 else this

inline fun Size.widthPx(scale: Scale, original: () -> Int): Int =
    if (isOriginal) original() else width.toPx(scale)

inline fun Size.heightPx(scale: Scale, original: () -> Int): Int =
    if (isOriginal) original() else height.toPx(scale)

fun Dimension.toPx(scale: Scale) =
    pxOrElse {
        when (scale) {
            Scale.FILL -> Int.MIN_VALUE
            Scale.FIT -> Int.MAX_VALUE
        }
    }
