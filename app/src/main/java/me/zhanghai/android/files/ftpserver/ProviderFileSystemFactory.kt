/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver

import org.apache.ftpserver.ftplet.FileSystemFactory
import org.apache.ftpserver.ftplet.FileSystemView
import org.apache.ftpserver.ftplet.User

class ProviderFileSystemFactory : FileSystemFactory {
    override fun createFileSystemView(user: User): FileSystemView = ProviderFileSystemView(user)
}
