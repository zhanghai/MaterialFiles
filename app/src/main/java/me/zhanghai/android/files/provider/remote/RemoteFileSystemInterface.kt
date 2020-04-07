/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote

import java8.nio.file.FileSystem

class RemoteFileSystemInterface(private val fileSystem: FileSystem) : IRemoteFileSystem.Stub() {
    override fun close(exception: ParcelableException) {
        tryRun(exception) { fileSystem.close() }
    }
}
