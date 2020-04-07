/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive

import java8.nio.file.Path
import java8.nio.file.ProviderMismatchException

val Path.archiveFile: Path
    get() {
        this as? ArchivePath ?: throw ProviderMismatchException(toString())
        return fileSystem.archiveFile
    }

fun Path.archiveRefresh() {
    this as? ArchivePath ?: throw ProviderMismatchException(toString())
    fileSystem.refresh()
}

fun Path.createArchiveRootPath(): Path =
    ArchiveFileSystemProvider.getOrNewFileSystem(this).rootDirectory
