/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root

import android.os.Parcelable
import java8.nio.file.FileStore
import java8.nio.file.FileSystem
import java8.nio.file.Path
import java8.nio.file.PathMatcher
import java8.nio.file.WatchService
import java8.nio.file.attribute.UserPrincipalLookupService
import java8.nio.file.spi.FileSystemProvider
import java.io.IOException

abstract class RootableFileSystem(
    localFileSystemCreator: (FileSystem) -> FileSystem,
    rootFileSystemCreator: (FileSystem) -> RootFileSystem
) : FileSystem(), Parcelable {
    protected open val localFileSystem: FileSystem = localFileSystemCreator(this)
    protected open val rootFileSystem: RootFileSystem = rootFileSystemCreator(this)

    override fun provider(): FileSystemProvider = localFileSystem.provider()

    @Throws(IOException::class)
    override fun close() {
        val wasOpen = localFileSystem.isOpen
        localFileSystem.close()
        // TODO: No need for this check?
        if (wasOpen) {
            rootFileSystem.close()
        }
    }

    override fun isOpen(): Boolean = localFileSystem.isOpen

    override fun isReadOnly(): Boolean = localFileSystem.isReadOnly

    override fun getSeparator(): String = localFileSystem.separator

    override fun getRootDirectories(): Iterable<Path> = localFileSystem.rootDirectories

    // TODO: Consider using root? But when?
    override fun getFileStores(): Iterable<FileStore> = localFileSystem.fileStores

    override fun supportedFileAttributeViews(): Set<String> =
        localFileSystem.supportedFileAttributeViews()

    override fun getPath(first: String, vararg more: String): Path =
        localFileSystem.getPath(first, *more)

    override fun getPathMatcher(syntaxAndPattern: String): PathMatcher =
        localFileSystem.getPathMatcher(syntaxAndPattern)

    override fun getUserPrincipalLookupService(): UserPrincipalLookupService =
        localFileSystem.userPrincipalLookupService

    // We don't have RemoteWatchService for now, and I doubt we can have one.
    @Throws(IOException::class)
    override fun newWatchService(): WatchService = localFileSystem.newWatchService()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as RootableFileSystem
        return localFileSystem == other.localFileSystem
    }

    override fun hashCode(): Int = localFileSystem.hashCode()
}
