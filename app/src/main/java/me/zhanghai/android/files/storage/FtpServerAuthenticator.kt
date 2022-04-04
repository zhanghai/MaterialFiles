/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import me.zhanghai.android.files.provider.ftp.client.Authenticator
import me.zhanghai.android.files.provider.ftp.client.Authority
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.valueCompat

object FtpServerAuthenticator : Authenticator {
    private val transientServers = mutableSetOf<FtpServer>()

    override fun getPassword(authority: Authority): String? {
        val server = synchronized(transientServers) {
            transientServers.find { it.authority == authority }
        } ?: Settings.STORAGES.valueCompat.find {
            it is FtpServer && it.authority == authority
        } as FtpServer?
        return server?.password
    }

    fun addTransientServer(server: FtpServer) {
        synchronized(transientServers) { transientServers += server }
    }

    fun removeTransientServer(server: FtpServer) {
        synchronized(transientServers) { transientServers -= server }
    }
}
