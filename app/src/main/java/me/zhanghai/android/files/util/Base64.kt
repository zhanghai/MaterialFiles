/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.util.Base64 as AndroidBase64

@JvmInline
value class Base64(val value: String)

fun String.asBase64(): Base64 = Base64(this)

fun Base64.toByteArray(): ByteArray = AndroidBase64.decode(value, AndroidBase64.DEFAULT)

fun ByteArray.toBase64(): Base64 =
    AndroidBase64.encodeToString(this, AndroidBase64.NO_WRAP).asBase64()
