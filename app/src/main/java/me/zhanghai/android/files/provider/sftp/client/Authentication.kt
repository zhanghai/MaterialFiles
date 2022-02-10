/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.sftp.client

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.common.Factory
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import net.schmizz.sshj.userauth.keyprovider.KeyProviderUtil
import net.schmizz.sshj.userauth.method.AuthMethod
import net.schmizz.sshj.userauth.method.AuthPassword
import net.schmizz.sshj.userauth.method.AuthPublickey
import net.schmizz.sshj.userauth.password.PasswordUtils
import java.io.IOException

sealed class Authentication : Parcelable {
    abstract fun toAuthMethod(): AuthMethod
}

@Parcelize
data class PasswordAuthentication(
    val password: String
) : Authentication() {
    override fun toAuthMethod(): AuthMethod =
        AuthPassword(PasswordUtils.createOneOff(password.toCharArray()))
}

@Parcelize
data class PublicKeyAuthentication(
    val privateKey: String
) : Authentication() {
    override fun toAuthMethod(): AuthMethod =
        AuthPublickey(privateKey.toKeyProvider())

    companion object {
        private val KEY_PROVIDER_FACTORIES = DefaultConfig().fileKeyProviderFactories

        fun validatePrivateKey(privateKey: String): Boolean =
            try {
                privateKey.toKeyProvider().private
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }

        /**
         * @see net.schmizz.sshj.SSHClient.loadKeys
         */
        @Throws(IOException::class)
        private fun String.toKeyProvider(): KeyProvider {
            val format = KeyProviderUtil.detectKeyFileFormat(this, false)
            val keyProvider = Factory.Named.Util.create(KEY_PROVIDER_FACTORIES, format.toString())
                ?: throw IOException("No key provider factory found for $format")
            keyProvider.init(this, null)
            return keyProvider
        }
    }
}
