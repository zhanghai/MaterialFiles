/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import me.zhanghai.android.files.provider.smb.client.Authentication
import me.zhanghai.android.files.provider.smb.client.Authenticator
import me.zhanghai.android.files.provider.smb.client.Authority
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.valueCompat

object SmbServerAuthenticator : Authenticator {
    private val transientServers = mutableSetOf<SmbServer>()

    override fun getAuthentication(authority: Authority): Authentication? {
        val server = synchronized(transientServers) {
            transientServers.find { it.authority == authority }
        } ?: Settings.STORAGES.valueCompat.find {
            it is SmbServer && it.authority == authority
        } as SmbServer?
        return server?.authentication
    }

    fun addTransientServer(server: SmbServer) {
        synchronized(transientServers) {
            transientServers += server
        }
    }

    fun removeTransientServer(server: SmbServer) {
        synchronized(transientServers) {
            transientServers -= server
        }
    }
}
