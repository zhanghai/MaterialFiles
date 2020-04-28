/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.media.MediaMetadataRetriever
import android.os.Build
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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
