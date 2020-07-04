/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb.client

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Authority(
    val host: String,
    val port: Int
) : Parcelable {
    override fun toString(): String = "$host:$port"
}
