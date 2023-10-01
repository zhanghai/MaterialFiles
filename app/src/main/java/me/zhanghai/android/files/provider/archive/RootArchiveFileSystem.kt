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
    private var passwords = listOf<String>()

    private var isSetPasswordNeeded = false

    private var isRefreshNeeded = false

    private val lock = Any()

    fun addPassword(password: String) {
        synchronized(lock) {
            passwords += password
            isSetPasswordNeeded = true
        }
    }

    fun setPasswords(passwords: List<String>) {
        synchronized(lock) {
            this.passwords = passwords
            isSetPasswordNeeded = true
        }
    }

    fun refresh() {
        synchronized(lock) {
            isRefreshNeeded = true
        }
    }

    @Throws(RemoteFileSystemException::class)
    fun prepare() {
        synchronized(lock) {
            if (isSetPasswordNeeded) {
                RootFileService.setArchivePasswords(fileSystem, passwords)
                isSetPasswordNeeded = false
            }
            if (isRefreshNeeded) {
                RootFileService.refreshArchiveFileSystem(fileSystem)
                isRefreshNeeded = false
            }
        }
    }
}
