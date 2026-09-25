/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.storage

import androidx.core.content.edit
import kotlinx.coroutines.runBlocking
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.BackgroundActivityStarter
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.app.defaultSharedPreferences
import me.zhanghai.android.files.provider.sftp.client.Authentication
import me.zhanghai.android.files.provider.sftp.client.Authenticator
import me.zhanghai.android.files.provider.sftp.client.Authority
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.createIntent
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.valueCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object SftpServerAuthenticator : Authenticator {
    private const val HOST_KEY_PREFIX = "sftp_host_key_"

    private val transientServers = mutableSetOf<SftpServer>()
    private val hostKeyLock = Any()

    override fun getAuthentication(authority: Authority): Authentication? {
        val server = synchronized(transientServers) {
            transientServers.find { it.authority == authority }
        } ?: Settings.STORAGES.valueCompat.find {
            it is SftpServer && it.authority == authority
        } as SftpServer?
        return server?.authentication
    }

    override fun getHostKey(authority: Authority): String? = synchronized(hostKeyLock) {
        defaultSharedPreferences.getString(authority.toHostKeyPreferenceKey(), null)
    }

    override fun putHostKey(authority: Authority, hostKey: String) {
        synchronized(hostKeyLock) {
            defaultSharedPreferences.edit { putString(authority.toHostKeyPreferenceKey(), hostKey) }
        }
    }

    override fun confirmChangedHostKey(
        authority: Authority,
        oldHostKey: String,
        newHostKey: String
    ): Boolean = runBlocking {
        suspendCoroutine { continuation ->
            val authorityString = authority.toString()
            val onTrustDecision: (Boolean) -> Unit = { trust ->
                if (trust) {
                    putHostKey(authority, newHostKey)
                }
                continuation.resume(trust)
            }

            BackgroundActivityStarter.startActivity(
                SftpHostKeyChangedDialogActivity::class.createIntent().putArgs(
                    SftpHostKeyChangedDialogFragment.Args(
                        authorityString, oldHostKey, newHostKey, onTrustDecision
                    )
                ),
                application.getString(R.string.storage_sftp_host_key_changed_title),
                SftpHostKeyChangedDialogFragment.getMessage(authorityString, application),
                application
            )
        }
    }

    fun addTransientServer(server: SftpServer) {
        synchronized(transientServers) { transientServers += server }
    }

    fun removeTransientServer(server: SftpServer) {
        synchronized(transientServers) { transientServers -= server }
    }

    private fun Authority.toHostKeyPreferenceKey(): String = "$HOST_KEY_PREFIX$this"
}
