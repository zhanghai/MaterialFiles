/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ftpserver

import java8.nio.file.Paths
import me.zhanghai.android.files.provider.archive.isArchivePath
import org.apache.ftpserver.ftplet.FileSystemView
import org.apache.ftpserver.ftplet.User
import java.net.URI

class ProviderFileSystemView(private val user: User) : FileSystemView {
    private val homeDirectory: ProviderFtpFile
    private var workingDirectory: ProviderFtpFile

    init {
        val homeDirectoryPath = Paths.get(URI.create(user.homeDirectory))
        homeDirectory = ProviderFtpFile(
            homeDirectoryPath, homeDirectoryPath.relativize(homeDirectoryPath), user
        )
        workingDirectory = homeDirectory
    }

    override fun getHomeDirectory(): ProviderFtpFile = homeDirectory

    override fun getWorkingDirectory(): ProviderFtpFile = workingDirectory

    override fun changeWorkingDirectory(directoryString: String): Boolean {
        val directory = getFile(directoryString)
        if (!directory.isDirectory) {
            return false
        }
        workingDirectory = directory
        return true
    }

    override fun getFile(fileString: String): ProviderFtpFile {
        val isAbsolute = fileString.startsWith("/")
        val homeDirectoryPath = homeDirectory.physicalFile
        val parentPath = if (isAbsolute) homeDirectoryPath else workingDirectory.physicalFile
        val relativeFileString = if (isAbsolute) fileString.drop(1) else fileString
        val filePath = parentPath.resolve(relativeFileString).normalize()
        if (!filePath.startsWith(homeDirectoryPath)) {
            return homeDirectory
        }
        return ProviderFtpFile(filePath, homeDirectoryPath.relativize(filePath), user)
    }

    override fun isRandomAccessible(): Boolean =
        // TODO: Better way of determining if the provider is random accessible.
        !homeDirectory.physicalFile.isArchivePath

    override fun dispose() {}
}
