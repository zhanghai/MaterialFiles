/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.mediastore

import java8.nio.channels.FileChannel
import java8.nio.channels.SeekableByteChannel
import java8.nio.file.AccessMode
import java8.nio.file.CopyOption
import java8.nio.file.DirectoryStream
import java8.nio.file.FileStore
import java8.nio.file.FileSystem
import java8.nio.file.FileSystemAlreadyExistsException
import java8.nio.file.LinkOption
import java8.nio.file.OpenOption
import java8.nio.file.Path
import java8.nio.file.ProviderMismatchException
import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.attribute.FileAttribute
import java8.nio.file.attribute.FileAttributeView
import java8.nio.file.spi.FileSystemProvider
import me.zhanghai.android.files.provider.common.PathListDirectoryStream
import me.zhanghai.android.files.provider.common.decodedPathByteString
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URI

object MediaStoreFileSystemProvider : FileSystemProvider() {
    const val SCHEME = "mediastore"

    internal val fileSystem = MediaStoreFileSystem(this)

    override fun getScheme(): String = SCHEME

    override fun newFileSystem(uri: URI, env: Map<String, *>): FileSystem {
        uri.requireSameScheme()
        throw FileSystemAlreadyExistsException()
    }

    override fun getFileSystem(uri: URI): FileSystem {
        uri.requireSameScheme()
        return fileSystem
    }

    override fun getPath(uri: URI): Path {
        uri.requireSameScheme()
        val segment = uri.decodedPathByteString?.toString()?.trimStart('/')
            ?: throw IllegalArgumentException("URI has no path: $uri")
        val category = MediaStoreCategory.fromSegment(segment)
            ?: throw IllegalArgumentException("Unknown MediaStore category: $segment")
        return fileSystem.getCategoryPath(category)
    }

    fun getCategoryPath(category: MediaStoreCategory): MediaStorePath =
        fileSystem.getCategoryPath(category)

    private fun URI.requireSameScheme() {
        val scheme = scheme
        require(scheme == SCHEME) { "URI scheme $scheme must be $SCHEME" }
    }

    @Throws(IOException::class)
    override fun newInputStream(file: Path, vararg options: OpenOption): InputStream {
        file as? MediaStorePath ?: throw ProviderMismatchException(file.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun newOutputStream(file: Path, vararg options: OpenOption): OutputStream {
        file as? MediaStorePath ?: throw ProviderMismatchException(file.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun newFileChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): FileChannel {
        file as? MediaStorePath ?: throw ProviderMismatchException(file.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun newByteChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): SeekableByteChannel {
        file as? MediaStorePath ?: throw ProviderMismatchException(file.toString())
        throw UnsupportedOperationException()
    }

    override fun newDirectoryStream(
        directory: Path,
        filter: DirectoryStream.Filter<in Path>
    ): DirectoryStream<Path> {
        directory as? MediaStorePath ?: throw ProviderMismatchException(directory.toString())
        val paths = MediaStoreLister.query(directory.category)
        return PathListDirectoryStream(paths, filter)
    }

    override fun createDirectory(directory: Path, vararg attributes: FileAttribute<*>) {
        directory as? MediaStorePath ?: throw ProviderMismatchException(directory.toString())
        throw UnsupportedOperationException()
    }

    override fun createSymbolicLink(
        link: Path,
        target: Path,
        vararg attributes: FileAttribute<*>
    ) {
        link as? MediaStorePath ?: throw ProviderMismatchException(link.toString())
        throw UnsupportedOperationException()
    }

    override fun createLink(link: Path, existing: Path) {
        link as? MediaStorePath ?: throw ProviderMismatchException(link.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun delete(path: Path) {
        path as? MediaStorePath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    override fun readSymbolicLink(link: Path): Path {
        link as? MediaStorePath ?: throw ProviderMismatchException(link.toString())
        throw UnsupportedOperationException()
    }

    override fun copy(source: Path, target: Path, vararg options: CopyOption) {
        source as? MediaStorePath ?: throw ProviderMismatchException(source.toString())
        throw UnsupportedOperationException()
    }

    override fun move(source: Path, target: Path, vararg options: CopyOption) {
        source as? MediaStorePath ?: throw ProviderMismatchException(source.toString())
        throw UnsupportedOperationException()
    }

    override fun isSameFile(path: Path, path2: Path): Boolean {
        path as? MediaStorePath ?: throw ProviderMismatchException(path.toString())
        return path == path2
    }

    override fun isHidden(path: Path): Boolean {
        path as? MediaStorePath ?: throw ProviderMismatchException(path.toString())
        return false
    }

    override fun getFileStore(path: Path): FileStore {
        path as? MediaStorePath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun checkAccess(path: Path, vararg modes: AccessMode) {
        path as? MediaStorePath ?: throw ProviderMismatchException(path.toString())
        // Virtual roots always exist; no real access check needed.
    }

    override fun <V : FileAttributeView> getFileAttributeView(
        path: Path,
        type: Class<V>,
        vararg options: LinkOption
    ): V? {
        path as? MediaStorePath ?: throw ProviderMismatchException(path.toString())
        return null
    }

    @Throws(IOException::class)
    override fun <A : BasicFileAttributes> readAttributes(
        path: Path,
        type: Class<A>,
        vararg options: LinkOption
    ): A {
        path as? MediaStorePath ?: throw ProviderMismatchException(path.toString())
        if (!type.isAssignableFrom(MediaStoreRootAttributes::class.java)) {
            throw UnsupportedOperationException(type.toString())
        }
        @Suppress("UNCHECKED_CAST")
        return MediaStoreRootAttributes() as A
    }

    override fun readAttributes(
        path: Path,
        attributes: String,
        vararg options: LinkOption
    ): Map<String, Any> {
        path as? MediaStorePath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    override fun setAttribute(
        path: Path,
        attribute: String,
        value: Any,
        vararg options: LinkOption
    ) {
        path as? MediaStorePath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }
}
