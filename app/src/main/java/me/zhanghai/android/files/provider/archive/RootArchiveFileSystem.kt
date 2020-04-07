/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive

import java8.nio.file.FileSystem
import me.zhanghai.android.files.provider.remote.RemoteFileSystemException
import me.zhanghai.android.files.provider.root.RootFileService
import me.zhanghai.android.files.provider.root.RootFileSystem

internal class RootArchiveFileSystem(
    private val fileSystem: FileSystem
) : RootFileSystem(fileSystem) {
    private var isRefreshNeeded = false

    private val lock = Any()

    fun refresh() {
        synchronized(lock) {
            if (hasRemoteInterface()) {
                isRefreshNeeded = true
            }
        }
    }

    @Throws(RemoteFileSystemException::class)
    fun doRefreshIfNeeded() {
        synchronized(lock) {
            if (isRefreshNeeded) {
                if (hasRemoteInterface()) {
                    RootFileService.refreshArchiveFileSystem(fileSystem)
                }
                isRefreshNeeded = false
            }
        }
    }
}
