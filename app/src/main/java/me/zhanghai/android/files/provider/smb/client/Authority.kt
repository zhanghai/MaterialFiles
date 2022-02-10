/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb.client

import android.os.Parcelable
import com.hierynomus.smbj.SMBClient
import kotlinx.parcelize.Parcelize

@Parcelize
data class Authority(
    val host: String,
    val port: Int,
    val username: String,
    val domain: String?
) : Parcelable {
    override fun toString(): String = buildString {
        if (domain != null) {
            append(domain)
            append('\\')
        }
        if (username.isNotEmpty()) {
            append(username)
        }
        if (domain != null || username.isNotEmpty()) {
            append('@')
        }
        append(host)
        if (port != DEFAULT_PORT) {
            append(port)
        }
    }

    companion object {
        const val DEFAULT_PORT = SMBClient.DEFAULT_PORT
    }
}
