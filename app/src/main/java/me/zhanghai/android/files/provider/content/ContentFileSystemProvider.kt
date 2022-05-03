/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content

import android.content.ContentResolver
import android.os.Build
import java8.nio.channels.FileChannel
import java8.nio.channels.SeekableByteChannel
import java8.nio.file.AccessDeniedException
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
import java8.nio.file.StandardOpenOption
import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.attribute.FileAttribute
import java8.nio.file.attribute.FileAttributeView
import java8.nio.file.spi.FileSystemProvider
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.provider.common.ByteStringPath
import me.zhanghai.android.files.provider.common.PathObservable
import me.zhanghai.android.files.provider.common.PathObservableProvider
import me.zhanghai.android.files.provider.common.open
import me.zhanghai.android.files.provider.common.toAccessModes
import me.zhanghai.android.files.provider.common.toOpenOptions
import me.zhanghai.android.files.provider.content.resolver.Resolver
import me.zhanghai.android.files.provider.content.resolver.ResolverException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URI

object ContentFileSystemProvider : FileSystemProvider(), PathObservableProvider {
    private const val SCHEME = ContentResolver.SCHEME_CONTENT

    internal val fileSystem = ContentFileSystem(this)

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
        return fileSystem.getPath(uri.toString())
    }

    private fun URI.requireSameScheme() {
        val scheme = scheme
        require(scheme == SCHEME) { "URI scheme $scheme must be $SCHEME" }
    }

    @Throws(IOException::class)
    override fun newInputStream(file: Path, vararg options: OpenOption): InputStream {
        file as? ContentPath ?: throw ProviderMismatchException(file.toString())
        val uri = file.uri!!
        val openOptions = options.toOpenOptions()
        if (openOptions.write) {
            throw UnsupportedOperationException(StandardOpenOption.WRITE.toString())
        }
        if (openOptions.append) {
            throw UnsupportedOperationException(StandardOpenOption.APPEND.toString())
        }
        val mode = openOptions.toContentMode()
        return try {
            Resolver.openInputStream(uri, mode)
        } catch (e: ResolverException) {
            throw e.toFileSystemException(file.toString())
        }
    }

    @Throws(IOException::class)
    override fun newOutputStream(file: Path, vararg options: OpenOption): OutputStream {
        file as? ContentPath ?: throw ProviderMismatchException(file.toString())
        val uri = file.uri!!
        val optionsSet = mutableSetOf(*options)
        if (optionsSet.isEmpty()) {
            optionsSet += StandardOpenOption.CREATE
            optionsSet += StandardOpenOption.TRUNCATE_EXISTING
        }
        optionsSet += StandardOpenOption.WRITE
        val openOptions = optionsSet.toOpenOptions()
        val mode = openOptions.toContentMode()
        return try {
            Resolver.openOutputStream(uri, mode)
        } catch (e: ResolverException) {
            throw e.toFileSystemException(file.toString())
        }
    }

    @Throws(IOException::class)
    override fun newFileChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): FileChannel {
        file as? ContentPath ?: throw ProviderMismatchException(file.toString())
        val uri = file.uri!!
        val openOptions = options.toOpenOptions()
        val mode = openOptions.toContentMode()
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(attributes.contentToString())
        }
        val pfd = try {
            Resolver.openParcelFileDescriptor(uri, mode)
        } catch (e: ResolverException) {
            throw e.toFileSystemException(file.toString())
        }
        return FileChannel::class.open(pfd, mode)
    }

    @Throws(IOException::class)
    override fun newByteChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): SeekableByteChannel = newFileChannel(file, options, *attributes)

    override fun newDirectoryStream(
        directory: Path,
        filter: DirectoryStream.Filter<in Path>
    ): DirectoryStream<Path> {
        directory as? ContentPath ?: throw ProviderMismatchException(directory.toString())
        throw UnsupportedOperationException()
    }

    override fun createDirectory(directory: Path, vararg attributes: FileAttribute<*>) {
        directory as? ContentPath ?: throw ProviderMismatchException(directory.toString())
        throw UnsupportedOperationException()
    }

    override fun createSymbolicLink(link: Path, target: Path, vararg attributes: FileAttribute<*>) {
        link as? ContentPath ?: throw ProviderMismatchException(link.toString())
        when (target) {
            is ContentPath, is ByteStringPath -> {}
            else -> throw ProviderMismatchException(target.toString())
        }
        throw UnsupportedOperationException()
    }

    override fun createLink(link: Path, existing: Path) {
        link as? ContentPath ?: throw ProviderMismatchException(link.toString())
        existing as? ContentPath ?: throw ProviderMismatchException(existing.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun delete(path: Path) {
        path as? ContentPath ?: throw ProviderMismatchException(path.toString())
        val uri = path.uri!!
        try {
            Resolver.delete(uri)
        } catch (e: ResolverException) {
            throw e.toFileSystemException(path.toString())
        }
    }

    override fun readSymbolicLink(link: Path): Path {
        link as? ContentPath ?: throw ProviderMismatchException(link.toString())
        throw UnsupportedOperationException()
    }

    override fun copy(source: Path, target: Path, vararg options: CopyOption) {
        source as? ContentPath ?: throw ProviderMismatchException(source.toString())
        target as? ContentPath ?: throw ProviderMismatchException(target.toString())
        throw UnsupportedOperationException()
    }

    override fun move(source: Path, target: Path, vararg options: CopyOption) {
        source as? ContentPath ?: throw ProviderMismatchException(source.toString())
        target as? ContentPath ?: throw ProviderMismatchException(target.toString())
        throw UnsupportedOperationException()
    }

    override fun isSameFile(path: Path, path2: Path): Boolean {
        path as? ContentPath ?: throw ProviderMismatchException(path.toString())
        return path == path2
    }

    override fun isHidden(path: Path): Boolean {
        path as? ContentPath ?: throw ProviderMismatchException(path.toString())
        return false
    }

    override fun getFileStore(path: Path): FileStore {
        path as? ContentPath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun checkAccess(path: Path, vararg modes: AccessMode) {
        path as? ContentPath ?: throw ProviderMismatchException(path.toString())
        val uri = path.uri!!
        // This checks existence as well.
        val mimeType = try {
            Resolver.getMimeType(uri)
        } catch (e: ResolverException) {
            throw e.toFileSystemException(path.toString())
        }
        val isDirectory = mimeType == MimeType.DIRECTORY.value
        if (isDirectory) {
            // There's no elegant way to check access to a directory beyond its existence.
            return
        }
        val accessModes = modes.toAccessModes()
        if (accessModes.execute) {
            throw AccessDeniedException(path.toString())
        }
        if (accessModes.write) {
            try {
                // Before Android 10, ParcelFileDescriptor.parseMode() parses "w" as "wt", and we would
                // truncate the file to empty. So work around that with "wa" on older platforms.
                val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "w" else "wa"
                Resolver.openOutputStream(uri, mode).use {}
            } catch (e: ResolverException) {
                throw e.toFileSystemException(path.toString())
            }
        }
        if (accessModes.read) {
            try {
                Resolver.openInputStream(uri, "r").use {}
            } catch (e: ResolverException) {
                throw e.toFileSystemException(path.toString())
            }
        }
    }

    override fun <V : FileAttributeView> getFileAttributeView(
        path: Path,
        type: Class<V>,
        vararg options: LinkOption
    ): V? {
        if (!supportsFileAttributeView(type)) {
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return getFileAttributeView(path) as V
    }

    internal fun supportsFileAttributeView(type: Class<out FileAttributeView>): Boolean =
        type.isAssignableFrom(ContentFileAttributeView::class.java)

    @Throws(IOException::class)
    override fun <A : BasicFileAttributes> readAttributes(
        path: Path,
        type: Class<A>,
        vararg options: LinkOption
    ): A {
        if (!type.isAssignableFrom(ContentFileAttributes::class.java)) {
            throw UnsupportedOperationException(type.toString())
        }
        @Suppress("UNCHECKED_CAST")
        return getFileAttributeView(path).readAttributes() as A
    }

    private fun getFileAttributeView(path: Path): ContentFileAttributeView {
        path as? ContentPath ?: throw ProviderMismatchException(path.toString())
        return ContentFileAttributeView(path)
    }

    override fun readAttributes(
        path: Path,
        attributes: String,
        vararg options: LinkOption
    ): Map<String, Any> {
        path as? ContentPath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    override fun setAttribute(
        path: Path,
        attribute: String,
        value: Any,
        vararg options: LinkOption
    ) {
        path as? ContentPath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun observe(path: Path, intervalMillis: Long): PathObservable {
        path as? ContentPath ?: throw ProviderMismatchException(path.toString())
        val uri = path.uri!!
        return ContentPathObservable(uri, intervalMillis)
    }
}
