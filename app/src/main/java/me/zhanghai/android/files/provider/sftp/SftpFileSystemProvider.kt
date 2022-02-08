/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.sftp

import java8.nio.channels.FileChannel
import java8.nio.channels.SeekableByteChannel
import java8.nio.file.AccessMode
import java8.nio.file.CopyOption
import java8.nio.file.DirectoryStream
import java8.nio.file.FileStore
import java8.nio.file.FileSystem
import java8.nio.file.FileSystemAlreadyExistsException
import java8.nio.file.FileSystemNotFoundException
import java8.nio.file.LinkOption
import java8.nio.file.OpenOption
import java8.nio.file.Path
import java8.nio.file.ProviderMismatchException
import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.attribute.FileAttribute
import java8.nio.file.attribute.FileAttributeView
import java8.nio.file.spi.FileSystemProvider
import me.zhanghai.android.files.provider.common.ByteStringPath
import me.zhanghai.android.files.provider.common.PathListDirectoryStream
import me.zhanghai.android.files.provider.common.PathObservable
import me.zhanghai.android.files.provider.common.PathObservableProvider
import me.zhanghai.android.files.provider.common.PosixFileMode
import me.zhanghai.android.files.provider.common.Searchable
import me.zhanghai.android.files.provider.common.WalkFileTreeSearchable
import me.zhanghai.android.files.provider.common.WatchServicePathObservable
import me.zhanghai.android.files.provider.common.decodedPathByteString
import me.zhanghai.android.files.provider.common.toAccessModes
import me.zhanghai.android.files.provider.common.toByteString
import me.zhanghai.android.files.provider.common.toCopyOptions
import me.zhanghai.android.files.provider.common.toLinkOptions
import me.zhanghai.android.files.provider.common.toOpenOptions
import me.zhanghai.android.files.provider.sftp.client.Authority
import me.zhanghai.android.files.provider.sftp.client.Client
import me.zhanghai.android.files.provider.sftp.client.ClientException
import me.zhanghai.android.files.provider.sftp.client.SecurityProviderHelper
import me.zhanghai.android.files.util.enumSetOf
import net.schmizz.sshj.sftp.FileAttributes
import net.schmizz.sshj.sftp.OpenMode
import java.io.IOException
import java.net.URI

object SftpFileSystemProvider : FileSystemProvider(), PathObservableProvider, Searchable {
    private const val SCHEME = "sftp"

    private val HIDDEN_FILE_NAME_PREFIX = ".".toByteString()

    private val fileSystems = mutableMapOf<Authority, SftpFileSystem>()

    private val lock = Any()

    init {
        SecurityProviderHelper.init()
    }

    override fun getScheme(): String = SCHEME

    override fun newFileSystem(uri: URI, env: Map<String, *>): FileSystem {
        uri.requireSameScheme()
        val authority = Authority(uri.host, uri.portOrDefaultPort, uri.userInfo)
        synchronized(lock) {
            if (fileSystems[authority] != null) {
                throw FileSystemAlreadyExistsException(authority.toString())
            }
            return newFileSystemLocked(authority)
        }
    }

    internal fun getOrNewFileSystem(authority: Authority): SftpFileSystem =
        synchronized(lock) { fileSystems[authority] ?: newFileSystemLocked(authority) }

    private fun newFileSystemLocked(authority: Authority): SftpFileSystem {
        val fileSystem = SftpFileSystem(this, authority)
        fileSystems[authority] = fileSystem
        return fileSystem
    }

    override fun getFileSystem(uri: URI): FileSystem {
        uri.requireSameScheme()
        val authority = Authority(uri.host, uri.portOrDefaultPort, uri.userInfo)
        return synchronized(lock) { fileSystems[authority] }
            ?: throw FileSystemNotFoundException(authority.toString())
    }

    internal fun removeFileSystem(fileSystem: SftpFileSystem) {
        val authority = fileSystem.authority
        synchronized(lock) { fileSystems.remove(authority) }
    }

    override fun getPath(uri: URI): Path {
        uri.requireSameScheme()
        val authority = Authority(uri.host, uri.portOrDefaultPort, uri.userInfo)
        val path = uri.decodedPathByteString
            ?: throw IllegalArgumentException("URI must have a path")
        return getOrNewFileSystem(authority).getPath(path)
    }

    private fun URI.requireSameScheme() {
        val scheme = scheme
        require(scheme == SCHEME) { "URI scheme $scheme must be $SCHEME" }
    }

    private val URI.portOrDefaultPort: Int
        get() = if (port != -1) port else Authority.DEFAULT_PORT

    @Throws(IOException::class)
    override fun newFileChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): FileChannel {
        file as? SftpPath ?: throw ProviderMismatchException(file.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun newByteChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): SeekableByteChannel {
        file as? SftpPath ?: throw ProviderMismatchException(file.toString())
        val openOptions = options.toOpenOptions()
        val flags = openOptions.toSftpFlags()
        val sftpAttributes = (PosixFileMode.fromAttributes(attributes)
            ?: PosixFileMode.CREATE_FILE_DEFAULT).toSftpAttributes()
        return try {
            Client.openByteChannel(file, flags, sftpAttributes)
        } catch (e: ClientException) {
            throw e.toFileSystemException(file.toString())
        }
    }

    @Throws(IOException::class)
    override fun newDirectoryStream(
        directory: Path,
        filter: DirectoryStream.Filter<in Path>
    ): DirectoryStream<Path> {
        directory as? SftpPath ?: throw ProviderMismatchException(directory.toString())
        val paths = try {
            @Suppress("UNCHECKED_CAST")
            Client.scandir(directory) as List<Path>
        } catch (e: ClientException) {
            throw e.toFileSystemException(directory.toString())
        }
        return PathListDirectoryStream(paths, filter)
    }

    @Throws(IOException::class)
    override fun createDirectory(directory: Path, vararg attributes: FileAttribute<*>) {
        directory as? SftpPath ?: throw ProviderMismatchException(directory.toString())
        val sftpAttributes = (PosixFileMode.fromAttributes(attributes)
            ?: PosixFileMode.CREATE_DIRECTORY_DEFAULT).toSftpAttributes()
        try {
            Client.mkdir(directory, sftpAttributes)
        } catch (e: ClientException) {
            throw e.toFileSystemException(directory.toString())
        }
    }

    override fun createSymbolicLink(link: Path, target: Path, vararg attributes: FileAttribute<*>) {
        link as? SftpPath ?: throw ProviderMismatchException(link.toString())
        val targetString = when (target) {
            is SftpPath -> target.toString()
            is ByteStringPath -> target.toString()
            else -> throw ProviderMismatchException(target.toString())
        }
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(attributes.contentToString())
        }
        try {
            Client.symlink(link, targetString)
        } catch (e: ClientException) {
            throw e.toFileSystemException(link.toString(), targetString)
        }
    }

    override fun createLink(link: Path, existing: Path) {
        link as? SftpPath ?: throw ProviderMismatchException(link.toString())
        existing as? SftpPath ?: throw ProviderMismatchException(existing.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun delete(path: Path) {
        path as? SftpPath ?: throw ProviderMismatchException(path.toString())
        try {
            Client.remove(path)
        } catch (e: ClientException) {
            throw e.toFileSystemException(path.toString())
        }
    }

    override fun readSymbolicLink(link: Path): Path {
        link as? SftpPath ?: throw ProviderMismatchException(link.toString())
        val target = try {
            Client.readlink(link)
        } catch (e: ClientException) {
            throw e.toFileSystemException(link.toString())
        }
        return ByteStringPath(target.toByteString())
    }

    @Throws(IOException::class)
    override fun copy(source: Path, target: Path, vararg options: CopyOption) {
        source as? SftpPath ?: throw ProviderMismatchException(source.toString())
        target as? SftpPath ?: throw ProviderMismatchException(target.toString())
        val copyOptions = options.toCopyOptions()
        SftpCopyMove.copy(source, target, copyOptions)
    }

    @Throws(IOException::class)
    override fun move(source: Path, target: Path, vararg options: CopyOption) {
        source as? SftpPath ?: throw ProviderMismatchException(source.toString())
        target as? SftpPath ?: throw ProviderMismatchException(target.toString())
        val copyOptions = options.toCopyOptions()
        SftpCopyMove.move(source, target, copyOptions)
    }

    override fun isSameFile(path: Path, path2: Path): Boolean {
        path as? SftpPath ?: throw ProviderMismatchException(path.toString())
        return path == path2
    }

    override fun isHidden(path: Path): Boolean {
        path as? SftpPath ?: throw ProviderMismatchException(path.toString())
        val fileName = path.fileNameByteString ?: return false
        return fileName.startsWith(HIDDEN_FILE_NAME_PREFIX)
    }

    override fun getFileStore(path: Path): FileStore {
        path as? SftpPath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun checkAccess(path: Path, vararg modes: AccessMode) {
        path as? SftpPath ?: throw ProviderMismatchException(path.toString())
        val accessModes = modes.toAccessModes()
        if (accessModes.execute) {
            throw UnsupportedOperationException(AccessMode.EXECUTE.toString())
        }
        val flags = enumSetOf<OpenMode>().apply {
            if (accessModes.read) {
                this += OpenMode.READ
            }
            if (accessModes.write) {
                this += OpenMode.WRITE
            }
        }
        val file = try {
            Client.open(path, flags, FileAttributes.EMPTY)
        } catch (e: ClientException) {
            throw e.toFileSystemException(path.toString())
        }
        try {
            Client.close(file)
        } catch (e: ClientException) {
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
        return getFileAttributeView(path) as V
    }

    internal fun supportsFileAttributeView(type: Class<out FileAttributeView>): Boolean =
        type.isAssignableFrom(SftpFileAttributeView::class.java)

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
        return getFileAttributeView(path).readAttributes() as A
    }

    private fun getFileAttributeView(path: Path, vararg options: LinkOption): SftpFileAttributeView {
        path as? SftpPath ?: throw ProviderMismatchException(path.toString())
        val linkOptions = options.toLinkOptions()
        return SftpFileAttributeView(path, linkOptions.noFollowLinks)
    }

    override fun readAttributes(
        path: Path,
        attributes: String,
        vararg options: LinkOption
    ): Map<String, Any> {
        path as? SftpPath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    override fun setAttribute(
        path: Path,
        attribute: String,
        value: Any,
        vararg options: LinkOption
    ) {
        path as? SftpPath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun observe(path: Path, intervalMillis: Long): PathObservable {
        path as? SftpPath ?: throw ProviderMismatchException(path.toString())
        return WatchServicePathObservable(path, intervalMillis)
    }

    @Throws(IOException::class)
    override fun search(
        directory: Path,
        query: String,
        intervalMillis: Long,
        listener: (List<Path>) -> Unit
    ) {
        directory as? SftpPath ?: throw ProviderMismatchException(directory.toString())
        WalkFileTreeSearchable.search(directory, query, intervalMillis, listener)
    }
}
