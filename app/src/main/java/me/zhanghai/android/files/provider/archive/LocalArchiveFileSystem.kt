/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive

import java8.nio.file.ClosedFileSystemException
import java8.nio.file.FileStore
import java8.nio.file.FileSystem
import java8.nio.file.NoSuchFileException
import java8.nio.file.NotDirectoryException
import java8.nio.file.NotLinkException
import java8.nio.file.Path
import java8.nio.file.PathMatcher
import java8.nio.file.WatchService
import java8.nio.file.attribute.UserPrincipalLookupService
import java8.nio.file.spi.FileSystemProvider
import me.zhanghai.android.files.provider.archive.archiver.ArchiveReader
import me.zhanghai.android.files.provider.archive.archiver.ReadArchive
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.ByteStringBuilder
import me.zhanghai.android.files.provider.common.ByteStringListPathCreator
import me.zhanghai.android.files.provider.common.IsDirectoryException
import me.zhanghai.android.files.provider.common.toByteString
import me.zhanghai.android.libarchive.ArchiveException
import java.io.IOException
import java.io.InputStream

internal class LocalArchiveFileSystem(
    private val fileSystem: ArchiveFileSystem,
    private val provider: ArchiveFileSystemProvider,
    val archiveFile: Path
) : FileSystem(), ByteStringListPathCreator {
    val rootDirectory = ArchivePath(fileSystem, SEPARATOR_BYTE_STRING)

    init {
        if (!rootDirectory.isAbsolute) {
            throw AssertionError("Root directory $rootDirectory must be absolute")
        }
        if (rootDirectory.nameCount != 0) {
            throw AssertionError("Root directory $rootDirectory must contain no names")
        }
    }

    val defaultDirectory: ArchivePath
        get() = rootDirectory

    private val lock = Any()

    private var isOpen = true

    private var passwords = listOf<String>()

    private var isRefreshNeeded = true

    private var entries: Map<Path, ReadArchive.Entry>? = null

    private var tree: Map<Path, List<Path>>? = null

    @Throws(IOException::class)
    fun getEntry(path: Path): ReadArchive.Entry =
        synchronized(lock) {
            ensureEntriesLocked(path)
            getEntryLocked(path)
        }

    @Throws(IOException::class)
    private fun getEntryLocked(path: Path): ReadArchive.Entry =
        synchronized(lock) {
            entries!![path] ?: throw NoSuchFileException(path.toString())
        }

    @Throws(IOException::class)
    fun newInputStream(file: Path): InputStream =
        synchronized(lock) {
            ensureEntriesLocked(file)
            val entry = getEntryLocked(file)
            if (entry.isDirectory) {
                throw IsDirectoryException(file.toString())
            }
            val inputStream = try {
                ArchiveReader.newInputStream(archiveFile, passwords, entry)
            } catch (e: ArchiveException) {
                throw e.toFileSystemOrInterruptedIOException(file)
            } ?: throw NoSuchFileException(file.toString())
            ArchiveExceptionInputStream(inputStream, file)
        }

    @Throws(IOException::class)
    fun getDirectoryChildren(directory: Path): List<Path> =
        synchronized(lock) {
            ensureEntriesLocked(directory)
            val entry = getEntryLocked(directory)
            if (!entry.isDirectory) {
                throw NotDirectoryException(directory.toString())
            }
            tree!![directory]!!
        }

    @Throws(IOException::class)
    fun readSymbolicLink(link: Path): String =
        synchronized(lock) {
            ensureEntriesLocked(link)
            val entry = getEntryLocked(link)
            if (!entry.isSymbolicLink) {
                throw NotLinkException(link.toString())
            }
            entry.symbolicLinkTarget.orEmpty()
        }

    fun addPassword(password: String) {
        synchronized(lock) {
            if (!isOpen) {
                throw ClosedFileSystemException()
            }
            passwords += password
        }
    }

    fun setPasswords(passwords: List<String>) {
        synchronized(lock) {
            if (!isOpen) {
                throw ClosedFileSystemException()
            }
            this.passwords = passwords
        }
    }

    fun refresh() {
        synchronized(lock) {
            if (!isOpen) {
                throw ClosedFileSystemException()
            }
            isRefreshNeeded = true
        }
    }

    @Throws(IOException::class)
    private fun ensureEntriesLocked(file: Path) {
        if (!isOpen) {
            throw ClosedFileSystemException()
        }
        if (isRefreshNeeded) {
            val entriesAndTree = try {
                ArchiveReader.readEntries(archiveFile, passwords, rootDirectory)
            } catch (e: ArchiveException) {
                throw e.toFileSystemOrInterruptedIOException(file)
            }
            entries = entriesAndTree.first
            tree = entriesAndTree.second
            isRefreshNeeded = false
        }
    }

    override fun provider(): FileSystemProvider = provider

    override fun close() {
        synchronized(lock) {
            if (!isOpen) {
                return
            }
            provider.removeFileSystem(fileSystem)
            isRefreshNeeded = false
            entries = null
            tree = null
            isOpen = false
        }
    }

    override fun isOpen(): Boolean = synchronized(lock) { isOpen }

    override fun isReadOnly(): Boolean = true

    override fun getSeparator(): String = SEPARATOR_STRING

    override fun getRootDirectories(): Iterable<Path> = listOf(rootDirectory)

    override fun getFileStores(): Iterable<FileStore> {
        // TODO
        throw UnsupportedOperationException()
    }

    override fun supportedFileAttributeViews(): Set<String> =
        ArchiveFileAttributeView.SUPPORTED_NAMES

    override fun getPath(first: String, vararg more: String): ArchivePath {
        val path = ByteStringBuilder(first.toByteString())
            .apply { more.forEach { append(SEPARATOR).append(it.toByteString()) } }
            .toByteString()
        return ArchivePath(fileSystem, path)
    }

    override fun getPath(first: ByteString, vararg more: ByteString): ArchivePath {
        val path = ByteStringBuilder(first)
            .apply { more.forEach { append(SEPARATOR).append(it) } }
            .toByteString()
        return ArchivePath(fileSystem, path)
    }

    override fun getPathMatcher(syntaxAndPattern: String): PathMatcher {
        throw UnsupportedOperationException()
    }

    override fun getUserPrincipalLookupService(): UserPrincipalLookupService {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun newWatchService(): WatchService {
        // TODO
        throw UnsupportedOperationException()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as LocalArchiveFileSystem
        return archiveFile == other.archiveFile
    }

    override fun hashCode(): Int = archiveFile.hashCode()

    companion object {
        const val SEPARATOR = '/'.code.toByte()
        private val SEPARATOR_BYTE_STRING = SEPARATOR.toByteString()
        private const val SEPARATOR_STRING = SEPARATOR.toInt().toChar().toString()
    }
}
