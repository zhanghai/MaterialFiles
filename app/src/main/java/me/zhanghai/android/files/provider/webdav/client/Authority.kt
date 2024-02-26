/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.webdav.client

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.provider.common.UriAuthority

@Parcelize
data class Authority(
    val protocol: Protocol,
    val host: String,
    val port: Int,
    val username: String?
) : Parcelable {
    init {
        require(username == null || username.isNotEmpty()) { "Username cannot be empty" }
    }

    fun toUriAuthority(): UriAuthority {
        val uriPort = port.takeIf { it != protocol.defaultPort }
        return UriAuthority(username, host, uriPort)
    }

    override fun toString(): String = toUriAuthority().toString()
}
