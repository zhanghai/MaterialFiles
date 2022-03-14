/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.ftp.client

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPSClient

enum class Protocol(val scheme: String, val defaultPort: Int, val createClient: () -> FTPClient) {
    FTP("ftp", FTPClient.DEFAULT_PORT, ::FTPClient),
    FTPS("ftps", FTPSClient.DEFAULT_FTPS_PORT, { FTPSClient(true) }),
    FTPES("ftpes", FTPClient.DEFAULT_PORT, { FTPSClient(false) });

    companion object {
        val SCHEMES = values().map { it.scheme }

        fun fromScheme(scheme: String): Protocol =
            values().firstOrNull() { it.scheme == scheme } ?: throw IllegalArgumentException(scheme)
    }
}
