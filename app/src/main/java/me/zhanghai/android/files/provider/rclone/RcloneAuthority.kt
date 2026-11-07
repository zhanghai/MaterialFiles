/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.rclone

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.provider.common.UriAuthority

@Parcelize
data class RcloneAuthority(
    val remoteName: String
) : Parcelable {
    fun toUriAuthority(): UriAuthority = UriAuthority(null, remoteName, null)

    override fun toString(): String = remoteName
}
