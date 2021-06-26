/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux

import java8.nio.file.FileStore
import java8.nio.file.FileSystem
import java8.nio.file.Path
import java8.nio.file.PathMatcher
import java8.nio.file.WatchService
import java8.nio.file.spi.FileSystemProvider
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.ByteStringBuilder
import me.zhanghai.android.files.provider.common.ByteStringListPathCreator
import me.zhanghai.android.files.provider.common.toByteString
import me.zhanghai.android.files.util.takeIfNotEmpty
import java.io.IOException

internal class LocalLinuxFileSystem(
    private val fileSystem: LinuxFileSystem,
    private val provider: LinuxFileSystemProvider
) : FileSystem(), ByteStringListPathCreator {
    val rootDirectory = LinuxPath(fileSystem, SEPARATOR_BYTE_STRING)

    init {
        if (!rootDirectory.isAbsolute) {
            throw AssertionError("Root directory must be absolute")
        }
        if (rootDirectory.nameCount != 0) {
            throw AssertionError("Root directory must contain no names")
        }
    }

    val defaultDirectory = LinuxPath(
        fileSystem,
        System.getenv("user.dir")?.takeIfNotEmpty()?.toByteString() ?: SEPARATOR_BYTE_STRING
    )

    init {
        if (!defaultDirectory.isAbsolute) {
            throw AssertionError("Default directory must be absolute")
        }
    }

    override fun provider(): FileSystemProvider = provider

    override fun close() {
        throw UnsupportedOperationException()
    }

    override fun isOpen(): Boolean = true

    override fun isReadOnly(): Boolean = false

    override fun getSeparator(): String = SEPARATOR_STRING

    override fun getRootDirectories(): Iterable<Path> = listOf(rootDirectory)

    override fun getFileStores(): Iterable<FileStore> = LocalLinuxFileStore.getFileStores(this)

    override fun supportedFileAttributeViews(): Set<String> = LinuxFileAttributeView.SUPPORTED_NAMES

    override fun getPath(first: String, vararg more: String): LinuxPath {
        val path = ByteStringBuilder(first.toByteString())
            .apply { more.forEach { append(SEPARATOR).append(it.toByteString()) } }
            .toByteString()
        return LinuxPath(fileSystem, path)
    }

    override fun getPath(first: ByteString, vararg more: ByteString): LinuxPath {
        val path = ByteStringBuilder(first)
            .apply { more.forEach { append(SEPARATOR).append(it) } }
            .toByteString()
        return LinuxPath(fileSystem, path)
    }

    override fun getPathMatcher(syntaxAndPattern: String): PathMatcher {
        throw UnsupportedOperationException()
    }

    override fun getUserPrincipalLookupService(): LinuxUserPrincipalLookupService =
        LinuxUserPrincipalLookupService

    @Throws(IOException::class)
    override fun newWatchService(): WatchService = LocalLinuxWatchService()

    companion object {
        const val SEPARATOR = '/'.code.toByte()
        private val SEPARATOR_BYTE_STRING = SEPARATOR.toByteString()
        private const val SEPARATOR_STRING = SEPARATOR.toInt().toChar().toString()
    }
}
