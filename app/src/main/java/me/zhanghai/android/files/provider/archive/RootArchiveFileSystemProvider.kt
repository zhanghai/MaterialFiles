/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive

import java8.nio.file.AccessMode
import java8.nio.file.DirectoryStream
import java8.nio.file.OpenOption
import java8.nio.file.Path
import java8.nio.file.ProviderMismatchException
import me.zhanghai.android.files.provider.remote.RemoteFileSystemException
import me.zhanghai.android.files.provider.root.RootFileSystemProvider
import java.io.IOException
import java.io.InputStream

class RootArchiveFileSystemProvider(scheme: String) : RootFileSystemProvider(scheme) {
    @Throws(IOException::class)
    override fun newInputStream(file: Path, vararg options: OpenOption): InputStream {
        prepareFileSystem(file)
        return super.newInputStream(file, *options)
    }

    @Throws(IOException::class)
    override fun newDirectoryStream(
        directory: Path,
        filter: DirectoryStream.Filter<in Path>
    ): DirectoryStream<Path> {
        prepareFileSystem(directory)
        return super.newDirectoryStream(directory, filter)
    }

    @Throws(IOException::class)
    override fun readSymbolicLink(link: Path): Path {
        prepareFileSystem(link)
        return super.readSymbolicLink(link)
    }

    @Throws(IOException::class)
    override fun checkAccess(path: Path, vararg modes: AccessMode) {
        prepareFileSystem(path)
        super.checkAccess(path, *modes)
    }

    @Throws(RemoteFileSystemException::class)
    private fun prepareFileSystem(path: Path) {
        path as? ArchivePath ?: throw ProviderMismatchException(path.toString())
        val fileSystem = path.fileSystem
        fileSystem.ensureRootInterface()
        fileSystem.doRefreshIfNeededAsRoot()
    }

    @Throws(RemoteFileSystemException::class)
    internal fun doRefreshIfNeeded(path: Path) {
        path as? ArchivePath ?: throw ProviderMismatchException(path.toString())
        path.fileSystem.doRefreshIfNeededAsRoot()
    }
}
