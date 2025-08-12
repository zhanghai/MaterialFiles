/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.webdav.client

import android.os.Parcelable
import android.util.Log
import at.bitfire.dav4jvm.BasicDigestAuthHandler
import at.bitfire.dav4jvm.UrlUtils
import kotlinx.parcelize.Parcelize
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

sealed class Authentication : Parcelable {
    abstract fun createAuthenticatorInterceptor(authority: Authority): AuthenticatorInterceptor
}

interface AuthenticatorInterceptor : Authenticator, Interceptor

@Parcelize
data object NoneAuthentication : Authentication() {
    override fun createAuthenticatorInterceptor(authority: Authority): AuthenticatorInterceptor =
        object : AuthenticatorInterceptor {
            override fun authenticate(route: Route?, response: Response): Request? = null

            override fun intercept(chain: Interceptor.Chain): Response =
                chain.proceed(chain.request())
        }
}

@Parcelize
data class PasswordAuthentication(
    val password: String
) : Authentication() {
    override fun createAuthenticatorInterceptor(authority: Authority): AuthenticatorInterceptor =
        object : AuthenticatorInterceptor {
            private val basicDigestAuthHandler = BasicDigestAuthHandler(
                UrlUtils.hostToDomain(authority.host), authority.username, password
            )

            override fun authenticate(route: Route?, response: Response): Request? =
                basicDigestAuthHandler.authenticate(route, response)

            override fun intercept(chain: Interceptor.Chain): Response =
                basicDigestAuthHandler.intercept(chain)
        }
}

@Parcelize
data class AccessTokenAuthentication(
    val accessToken: String
) : Authentication() {
    override fun createAuthenticatorInterceptor(authority: Authority): AuthenticatorInterceptor =
        object : AuthenticatorInterceptor {
            override fun authenticate(route: Route?, response: Response): Request? = null

            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                val requestHost = request.url.host
                val domain = UrlUtils.hostToDomain(authority.host)
                if (!UrlUtils.hostToDomain(requestHost).equals(domain, true)) {
                    Log.w(
                        LOG_TAG,
                        "Not authenticating against $requestHost because it doesn't belong to " +
                            domain
                    )
                    return chain.proceed(request)
                }
                val newRequest = request.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
                return chain.proceed(newRequest)
            }
        }

    companion object {
        private val LOG_TAG = AccessTokenAuthentication::class.java.simpleName
    }
}
