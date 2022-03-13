/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.ftp.client

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.provider.common.UriAuthority
import me.zhanghai.android.files.util.takeIfNotEmpty

@Parcelize
data class Authority(
    val protocol: Protocol,
    val host: String,
    val port: Int,
    val username: String
) : Parcelable {
    fun toUriAuthority(): UriAuthority {
        val userInfo = username.takeIfNotEmpty()
        val uriPort = port.takeIf { it != protocol.defaultPort }
        return UriAuthority(userInfo, host, uriPort)
    }

    override fun toString(): String = toUriAuthority().toString()

    companion object {
        // @see https://www.rfc-editor.org/rfc/rfc1635
        const val ANONYMOUS_USERNAME = "anonymous"
        const val ANONYMOUS_PASSWORD = "guest"
    }
}
