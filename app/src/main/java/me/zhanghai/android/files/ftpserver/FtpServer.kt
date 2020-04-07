/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver

import java8.nio.file.Path
import org.apache.ftpserver.ConnectionConfigFactory
import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.ftplet.FtpException
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.usermanager.impl.BaseUser
import org.apache.ftpserver.usermanager.impl.WritePermission

class FtpServer(
    private val username: String,
    private val password: String?,
    private val port: Int,
    private val homeDirectory: Path,
    private val writable: Boolean
) {
    private lateinit var server: FtpServer

    @Throws(FtpException::class, RuntimeException::class)
    fun start() {
        server = FtpServerFactory()
            .apply {
                val listener = ListenerFactory()
                    .apply { port = this@FtpServer.port }
                    .createListener()
                addListener("default", listener)
                val user = BaseUser().apply {
                    name = username
                    password = this@FtpServer.password
                    authorities = if (writable) listOf(WritePermission()) else emptyList()
                    homeDirectory = this@FtpServer.homeDirectory.toUri().toString()
                }
                userManager.save(user)
                fileSystem = ProviderFileSystemFactory()
                connectionConfig = ConnectionConfigFactory()
                    .apply { isAnonymousLoginEnabled = true }
                    .createConnectionConfig()
            }
            .createServer()
        server.start()
    }

    fun stop() {
        server.stop()
    }
}
