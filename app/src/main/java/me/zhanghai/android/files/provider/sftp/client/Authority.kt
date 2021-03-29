/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.sftp.client

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import net.schmizz.sshj.SSHClient

@Parcelize
data class Authority(
    val host: String,
    val port: Int
) : Parcelable {
    override fun toString(): String = if (port != DEFAULT_PORT) "$host:$port" else host

    companion object {
        const val DEFAULT_PORT = SSHClient.DEFAULT_PORT
    }
}
