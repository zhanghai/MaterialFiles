/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.sftp.client

interface Authenticator {
    fun getAuthentication(authority: Authority): Authentication?

    fun getHostKey(authority: Authority): String?

    fun putHostKey(authority: Authority, hostKey: String)

    fun confirmChangedHostKey(
        authority: Authority,
        oldHostKey: String,
        newHostKey: String
    ): Boolean = false
}
