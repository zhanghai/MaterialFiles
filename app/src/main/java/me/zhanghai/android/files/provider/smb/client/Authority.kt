/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb.client

import android.os.Parcel
import android.os.Parcelable
import com.hierynomus.smbj.SMBClient
import me.zhanghai.android.files.provider.common.UriAuthority
import me.zhanghai.android.files.util.takeIfNotEmpty

data class Authority(
    val host: String,
    val port: Int,
    val username: String,
    val domain: String?,
    val encrypt: Boolean
) : Parcelable {
    fun toUriAuthority(): UriAuthority {
        val userInfo = if (domain != null) "$domain\\$username" else username.takeIfNotEmpty()
        val uriPort = port.takeIf { it != DEFAULT_PORT }
        return UriAuthority(userInfo, host, uriPort)
    }

    override fun toString(): String = toUriAuthority().toString()

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // A version marker is written first so that authorities persisted before the encryption
        // option was added (which started directly with the host name) can still be read back.
        dest.writeInt(PARCEL_VERSION_MARKER)
        dest.writeString(host)
        dest.writeInt(port)
        dest.writeString(username)
        dest.writeString(domain)
        dest.writeInt(if (encrypt) 1 else 0)
    }

    companion object {
        const val DEFAULT_PORT = SMBClient.DEFAULT_PORT
        const val DEFAULT_ENCRYPT = false

        // A negative marker that can never be mistaken for the (non-negative) host name string
        // length that legacy parcels began with, so older saved servers are read as unencrypted.
        private const val PARCEL_VERSION_MARKER = -0x536D6201

        @JvmField
        val CREATOR = object : Parcelable.Creator<Authority> {
            override fun createFromParcel(source: Parcel): Authority {
                val startPosition = source.dataPosition()
                return if (source.readInt() == PARCEL_VERSION_MARKER) {
                    Authority(
                        source.readString()!!, source.readInt(), source.readString()!!,
                        source.readString(), source.readInt() != 0
                    )
                } else {
                    // Legacy layout without the version marker and the encryption flag.
                    source.setDataPosition(startPosition)
                    Authority(
                        source.readString()!!, source.readInt(), source.readString()!!,
                        source.readString(), DEFAULT_ENCRYPT
                    )
                }
            }

            override fun newArray(size: Int): Array<Authority?> = arrayOfNulls(size)
        }
    }
}
