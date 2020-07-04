/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb.client

import android.os.Parcelable
import com.hierynomus.smbj.auth.AuthenticationContext
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Authentication(
    val username: String,
    val password: String,
    val domain: String?
) : Parcelable {
    fun toContext(): AuthenticationContext =
        AuthenticationContext(username, password.toCharArray(), domain)
}
