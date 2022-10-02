/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.annotation.RequiresApi
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass

val KClass<MediaMetadataRetriever>.METADATA_KEY_SAMPLERATE: Int
    @RequiresApi(Build.VERSION_CODES.Q)
    get() = 38

fun MediaMetadataRetriever.getFrameAtTimeCompat(
    timeUs: Long,
    option: Int,
    params: MediaMetadataRetriever.BitmapParams?
): Bitmap? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && params != null) {
        getFrameAtTime(timeUs, option, params)
    } else {
        getFrameAtTime(timeUs, option)
    }

@RequiresApi(Build.VERSION_CODES.O_MR1)
fun MediaMetadataRetriever.getScaledFrameAtTimeCompat(
    timeUs: Long,
    option: Int,
    dstWidth: Int,
    dstHeight: Int,
    params: MediaMetadataRetriever.BitmapParams?
): Bitmap? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && params != null) {
        getScaledFrameAtTime(timeUs, option, dstWidth, dstHeight, params)
    } else {
        getScaledFrameAtTime(timeUs, option, dstWidth, dstHeight)
    }

@OptIn(ExperimentalContracts::class)
inline fun <R> MediaMetadataRetriever.use(block: (MediaMetadataRetriever) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val autoCloseable: AutoCloseable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this
    } else {
        AutoCloseable { release() }
    }
    return autoCloseable.use { block(this) }
}
