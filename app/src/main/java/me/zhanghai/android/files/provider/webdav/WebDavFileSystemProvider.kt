/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.webdav

import at.bitfire.dav4jvm.exception.DavException
import java8.nio.channels.FileChannel
import java8.nio.channels.SeekableByteChannel
import java8.nio.file.AccessMode
import java8.nio.file.CopyOption
import java8.nio.file.DirectoryStream
import java8.nio.file.FileAlreadyExistsException
import java8.nio.file.FileStore
import java8.nio.file.FileSystem
import java8.nio.file.FileSystemAlreadyExistsException
import java8.nio.file.FileSystemException
import java8.nio.file.FileSystemNotFoundException
import java8.nio.file.LinkOption
import java8.nio.file.NoSuchFileException
import java8.nio.file.NotLinkException
import java8.nio.file.OpenOption
import java8.nio.file.Path
import java8.nio.file.ProviderMismatchException
import java8.nio.file.StandardOpenOption
import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.attribute.FileAttribute
import java8.nio.file.attribute.FileAttributeView
import java8.nio.file.spi.FileSystemProvider
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.ByteStringPath
import me.zhanghai.android.files.provider.common.DelegateSchemeFileSystemProvider
import me.zhanghai.android.files.provider.common.PathListDirectoryStream
import me.zhanghai.android.files.provider.common.PathObservable
import me.zhanghai.android.files.provider.common.PathObservableProvider
import me.zhanghai.android.files.provider.common.Searchable
import me.zhanghai.android.files.provider.common.WalkFileTreeSearchable
import me.zhanghai.android.files.provider.common.WatchServicePathObservable
import me.zhanghai.android.files.provider.common.decodedPathByteString
import me.zhanghai.android.files.provider.common.toAccessModes
import me.zhanghai.android.files.provider.common.toByteString
import me.zhanghai.android.files.provider.common.toCopyOptions
import me.zhanghai.android.files.provider.common.toLinkOptions
import me.zhanghai.android.files.provider.common.toOpenOptions
import me.zhanghai.android.files.provider.webdav.client.Authority
import me.zhanghai.android.files.provider.webdav.client.Client
import me.zhanghai.android.files.provider.webdav.client.Protocol
import me.zhanghai.android.files.provider.webdav.client.isSymbolicLink
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URI

object WebDavFileSystemProvider : FileSystemProvider(), PathObservableProvider, Searchable {
    private val HIDDEN_FILE_NAME_PREFIX = ".".toByteString()

    private val fileSystems = mutableMapOf<Authority, WebDavFileSystem>()

    private val lock = Any()

    override fun getScheme(): String = Protocol.DAV.scheme

    override fun newFileSystem(uri: URI, env: Map<String, *>): FileSystem {
        uri.requireSameScheme()
        val authority = uri.webDavAuthority
        synchronized(lock) {
            if (fileSystems[authority] != null) {
                throw FileSystemAlreadyExistsException(authority.toString())
            }
            return newFileSystemLocked(authority)
        }
    }

    internal fun getOrNewFileSystem(authority: Authority): WebDavFileSystem =
        synchronized(lock) { fileSystems[authority] ?: newFileSystemLocked(authority) }

    private fun newFileSystemLocked(authority: Authority): WebDavFileSystem {
        val fileSystem = WebDavFileSystem(this, authority)
        fileSystems[authority] = fileSystem
        return fileSystem
    }

    override fun getFileSystem(uri: URI): FileSystem {
        uri.requireSameScheme()
        val authority = uri.webDavAuthority
        return synchronized(lock) { fileSystems[authority] }
            ?: throw FileSystemNotFoundException(authority.toString())
    }

    internal fun removeFileSystem(fileSystem: WebDavFileSystem) {
        val authority = fileSystem.authority
        synchronized(lock) { fileSystems.remove(authority) }
    }

    override fun getPath(uri: URI): Path {
        uri.requireSameScheme()
        val authority = uri.webDavAuthority
        val path = uri.decodedPathByteString
            ?: throw IllegalArgumentException("URI must have a path")
        return getOrNewFileSystem(authority).getPath(path)
    }

    private fun URI.requireSameScheme() {
        val scheme = scheme
        require(scheme in Protocol.SCHEMES) { "URI scheme $scheme must be in ${Protocol.SCHEMES}" }
    }

    private val URI.webDavAuthority: Authority
        get() {
            val protocol = Protocol.fromScheme(scheme)
            val port = if (port != -1) port else protocol.defaultPort
            val username = userInfo.orEmpty()
            return Authority(protocol, host, port, username)
        }

    @Throws(IOException::class)
    override fun newInputStream(file: Path, vararg options: OpenOption): InputStream {
        file as? WebDavPath ?: throw ProviderMismatchException(file.toString())
        val openOptions = options.toOpenOptions()
        openOptions.checkForWebDav()
        if (openOptions.write) {
            throw UnsupportedOperationException(StandardOpenOption.WRITE.toString())
        }
        if (openOptions.append) {
            throw UnsupportedOperationException(StandardOpenOption.APPEND.toString())
        }
        if (openOptions.truncateExisting) {
            throw UnsupportedOperationException(StandardOpenOption.TRUNCATE_EXISTING.toString())
        }
        if (openOptions.create || openOptions.createNew || openOptions.noFollowLinks) {
            val fileResponse = try {
                Client.findPropertiesOrNull(file, true)
            } catch (e: DavException) {
                throw e.toFileSystemException(file.toString())
            }
            if (openOptions.noFollowLinks && fileResponse != null && fileResponse.isSymbolicLink) {
                throw FileSystemException(
                    file.toString(), null, "File is a symbolic link: $fileResponse"
                )
            }
            if (openOptions.createNew && fileResponse != null) {
                throw FileAlreadyExistsException(file.toString())
            }
            if ((openOptions.create || openOptions.createNew) && fileResponse == null) {
                try {
                    Client.makeFile(file)
                } catch (e: DavException) {
                    throw e.toFileSystemException(file.toString())
                }
            }
        }
        try {
            return Client.get(file)
        } catch (e: DavException) {
            throw e.toFileSystemException(file.toString())
        }
    }

    @Throws(IOException::class)
    override fun newOutputStream(file: Path, vararg options: OpenOption): OutputStream {
        file as? WebDavPath ?: throw ProviderMismatchException(file.toString())
        val optionsSet = mutableSetOf(*options)
        if (optionsSet.isEmpty()) {
            optionsSet += StandardOpenOption.CREATE
            optionsSet += StandardOpenOption.TRUNCATE_EXISTING
        }
        optionsSet += StandardOpenOption.WRITE
        val openOptions = optionsSet.toOpenOptions()
        openOptions.checkForWebDav()
        if (!openOptions.truncateExisting && !openOptions.createNew) {
            throw UnsupportedOperationException("Missing ${StandardOpenOption.TRUNCATE_EXISTING}")
        }
        val fileResponse = try {
            Client.findPropertiesOrNull(file, true)
        } catch (e: DavException) {
            throw e.toFileSystemException(file.toString())
        }
        if (openOptions.createNew && fileResponse != null) {
            throw FileAlreadyExistsException(file.toString())
        }
        if (!(openOptions.create || openOptions.createNew) && fileResponse == null) {
            throw NoSuchFileException(file.toString())
        }
        try {
            return Client.put(file)
        } catch (e: DavException) {
            throw e.toFileSystemException(file.toString())
        }
    }

    @Throws(IOException::class)
    override fun newFileChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): FileChannel {
        file as? WebDavPath ?: throw ProviderMismatchException(file.toString())
        options.toOpenOptions().checkForWebDav()
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(attributes.contentToString())
        }
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun newByteChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): SeekableByteChannel {
        file as? WebDavPath ?: throw ProviderMismatchException(file.toString())
        val openOptions = options.toOpenOptions()
        openOptions.checkForWebDav()
        if (openOptions.write && !openOptions.truncateExisting) {
            throw UnsupportedOperationException("Missing ${StandardOpenOption.TRUNCATE_EXISTING}")
        }
        if (openOptions.write || openOptions.create || openOptions.createNew ||
            openOptions.noFollowLinks) {
            val fileResponse = try {
                Client.findPropertiesOrNull(file, true)
            } catch (e: DavException) {
                throw e.toFileSystemException(file.toString())
            }
            if (openOptions.createNew && fileResponse != null) {
                throw FileAlreadyExistsException(file.toString())
            }
            if (openOptions.noFollowLinks && fileResponse != null && fileResponse.isSymbolicLink) {
                throw FileSystemException(
                    file.toString(), null, "File is a symbolic link: $fileResponse"
                )
            }
            if (fileResponse == null) {
                if (!(openOptions.create || openOptions.createNew)) {
                    throw NoSuchFileException(file.toString())
                }
                try {
                    Client.makeFile(file)
                } catch (e: DavException) {
                    throw e.toFileSystemException(file.toString())
                }
            }
        }
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(attributes.contentToString())
        }
        try {
            return Client.openByteChannel(file, openOptions.append)
        } catch (e: DavException) {
            throw e.toFileSystemException(file.toString())
        }
    }

    @Throws(IOException::class)
    override fun newDirectoryStream(
        directory: Path,
        filter: DirectoryStream.Filter<in Path>
    ): DirectoryStream<Path> {
        directory as? WebDavPath ?: throw ProviderMismatchException(directory.toString())
        val paths = try {
            @Suppress("UNCHECKED_CAST")
            Client.findCollectionMembers(directory) as List<Path>
        } catch (e: DavException) {
            throw e.toFileSystemException(directory.toString())
        }
        return PathListDirectoryStream(paths, filter)
    }

    @Throws(IOException::class)
    override fun createDirectory(directory: Path, vararg attributes: FileAttribute<*>) {
        directory as? WebDavPath ?: throw ProviderMismatchException(directory.toString())
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(attributes.contentToString())
        }
        try {
            Client.makeCollection(directory)
        } catch (e: DavException) {
            throw e.toFileSystemException(directory.toString())
        }
    }

    override fun createSymbolicLink(link: Path, target: Path, vararg attributes: FileAttribute<*>) {
        link as? WebDavPath ?: throw ProviderMismatchException(link.toString())
        when (target) {
            is WebDavPath, is ByteStringPath -> {}
            else -> throw ProviderMismatchException(target.toString())
        }
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(attributes.contentToString())
        }
        throw UnsupportedOperationException()
    }

    override fun createLink(link: Path, existing: Path) {
        link as? WebDavPath ?: throw ProviderMismatchException(link.toString())
        existing as? WebDavPath ?: throw ProviderMismatchException(existing.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun delete(path: Path) {
        path as? WebDavPath ?: throw ProviderMismatchException(path.toString())
        try {
            Client.delete(path)
        } catch (e: DavException) {
            throw e.toFileSystemException(path.toString())
        }
    }

    override fun readSymbolicLink(link: Path): Path {
        link as? WebDavPath ?: throw ProviderMismatchException(link.toString())
        val linkResponse = try {
            Client.findProperties(link, true)
        } catch (e: DavException) {
            throw e.toFileSystemException(link.toString())
        }
        val target = linkResponse.newLocation?.toString()
            ?: throw NotLinkException(link.toString(), null, linkResponse.toString())
        // TODO: Convert to webdav(s) scheme?
        return ByteStringPath(ByteString.fromString(target))
    }

    @Throws(IOException::class)
    override fun copy(source: Path, target: Path, vararg options: CopyOption) {
        source as? WebDavPath ?: throw ProviderMismatchException(source.toString())
        target as? WebDavPath ?: throw ProviderMismatchException(target.toString())
        val copyOptions = options.toCopyOptions()
        WebDavCopyMove.copy(source, target, copyOptions)
    }

    @Throws(IOException::class)
    override fun move(source: Path, target: Path, vararg options: CopyOption) {
        source as? WebDavPath ?: throw ProviderMismatchException(source.toString())
        target as? WebDavPath ?: throw ProviderMismatchException(target.toString())
        val copyOptions = options.toCopyOptions()
        WebDavCopyMove.move(source, target, copyOptions)
    }

    override fun isSameFile(path: Path, path2: Path): Boolean {
        path as? WebDavPath ?: throw ProviderMismatchException(path.toString())
        return path == path2
    }

    override fun isHidden(path: Path): Boolean {
        path as? WebDavPath ?: throw ProviderMismatchException(path.toString())
        val fileName = path.fileNameByteString ?: return false
        return fileName.startsWith(HIDDEN_FILE_NAME_PREFIX)
    }

    override fun getFileStore(path: Path): FileStore {
        path as? WebDavPath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun checkAccess(path: Path, vararg modes: AccessMode) {
        path as? WebDavPath ?: throw ProviderMismatchException(path.toString())
        val accessModes = modes.toAccessModes()
        if (accessModes.write) {
            throw UnsupportedOperationException(AccessMode.WRITE.toString())
        }
        if (accessModes.execute) {
            throw UnsupportedOperationException(AccessMode.EXECUTE.toString())
        }
        // Assume the file can be read if it can be listed.
        try {
            Client.findProperties(path, false)
        } catch (e: DavException) {
            throw e.toFileSystemException(path.toString())
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
        return getFileAttributeView(path, *options) as V
    }

    internal fun supportsFileAttributeView(type: Class<out FileAttributeView>): Boolean =
        type.isAssignableFrom(WebDavFileAttributeView::class.java)

    @Throws(IOException::class)
    override fun <A : BasicFileAttributes> readAttributes(
        path: Path,
        type: Class<A>,
        vararg options: LinkOption
    ): A {
        if (!type.isAssignableFrom(BasicFileAttributes::class.java)) {
            throw UnsupportedOperationException(type.toString())
        }
        @Suppress("UNCHECKED_CAST")
        return getFileAttributeView(path, *options).readAttributes() as A
    }

    private fun getFileAttributeView(path: Path, vararg options: LinkOption): WebDavFileAttributeView {
        path as? WebDavPath ?: throw ProviderMismatchException(path.toString())
        val linkOptions = options.toLinkOptions()
        return WebDavFileAttributeView(path, linkOptions.noFollowLinks)
    }

    override fun readAttributes(
        path: Path,
        attributes: String,
        vararg options: LinkOption
    ): Map<String, Any> {
        path as? WebDavPath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    override fun setAttribute(
        path: Path,
        attribute: String,
        value: Any,
        vararg options: LinkOption
    ) {
        path as? WebDavPath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun observe(path: Path, intervalMillis: Long): PathObservable {
        path as? WebDavPath ?: throw ProviderMismatchException(path.toString())
        return WatchServicePathObservable(path, intervalMillis)
    }

    @Throws(IOException::class)
    override fun search(
        directory: Path,
        query: String,
        intervalMillis: Long,
        listener: (List<Path>) -> Unit
    ) {
        directory as? WebDavPath ?: throw ProviderMismatchException(directory.toString())
        WalkFileTreeSearchable.search(directory, query, intervalMillis, listener)
    }
}

val WebDavsFileSystemProvider =
    DelegateSchemeFileSystemProvider(Protocol.DAVS.scheme, WebDavFileSystemProvider)
