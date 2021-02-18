/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb.client

import android.os.Parcelable
import com.hierynomus.smbj.auth.AuthenticationContext
import kotlinx.parcelize.Parcelize

@Parcelize
data class Authentication(
    val username: String,
    val domain: String?,
    val password: String
) : Parcelable {
    fun toContext(): AuthenticationContext =
        AuthenticationContext(username, password.toCharArray(), domain)

    companion object {
        val GUEST = AuthenticationContext.guest().toAuthentication()
        val ANONYMOUS = AuthenticationContext.anonymous().toAuthentication()

        private fun AuthenticationContext.toAuthentication(): Authentication =
            Authentication(username, domain, password.concatToString())
    }
}
