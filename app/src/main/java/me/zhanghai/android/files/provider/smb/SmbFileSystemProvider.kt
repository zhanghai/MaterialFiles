/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb

import com.hierynomus.msdtyp.AccessMask
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
import me.zhanghai.android.files.provider.common.CloseableIterator
import me.zhanghai.android.files.provider.common.InvalidFileNameException
import me.zhanghai.android.files.provider.common.PathIteratorDirectoryStream
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
import me.zhanghai.android.files.provider.smb.client.Authority
import me.zhanghai.android.files.provider.smb.client.Client
import me.zhanghai.android.files.provider.smb.client.ClientException
import me.zhanghai.android.files.provider.smb.client.FileInformation
import me.zhanghai.android.files.provider.smb.client.SymbolicLinkReparseData
import me.zhanghai.android.files.util.enumSetOf
import me.zhanghai.android.files.util.takeIfNotEmpty
import java.io.IOException
import java.net.URI

object SmbFileSystemProvider : FileSystemProvider(), PathObservableProvider, Searchable {
    private const val SCHEME = "smb"

    private val HIDDEN_FILE_NAME_PREFIX = ".".toByteString()

    private val fileSystems = mutableMapOf<Authority, SmbFileSystem>()

    private val lock = Any()

    override fun getScheme(): String = SCHEME

    override fun newFileSystem(uri: URI, env: Map<String, *>): FileSystem {
        uri.requireSameScheme()
        val authority = uri.smbAuthority
        synchronized(lock) {
            if (fileSystems[authority] != null) {
                throw FileSystemAlreadyExistsException(authority.toString())
            }
            return newFileSystemLocked(authority)
        }
    }

    internal fun getOrNewFileSystem(authority: Authority): SmbFileSystem =
        synchronized(lock) { fileSystems[authority] ?: newFileSystemLocked(authority) }

    private fun newFileSystemLocked(authority: Authority): SmbFileSystem {
        val fileSystem = SmbFileSystem(this, authority)
        fileSystems[authority] = fileSystem
        return fileSystem
    }

    override fun getFileSystem(uri: URI): FileSystem {
        uri.requireSameScheme()
        val authority = uri.smbAuthority
        return synchronized(lock) { fileSystems[authority] }
            ?: throw FileSystemNotFoundException(authority.toString())
    }

    internal fun removeFileSystem(fileSystem: SmbFileSystem) {
        val authority = fileSystem.authority
        synchronized(lock) { fileSystems.remove(authority) }
    }

    override fun getPath(uri: URI): Path {
        uri.requireSameScheme()
        val authority = uri.smbAuthority
        val path = uri.decodedPathByteString
            ?: throw IllegalArgumentException("URI must have a path")
        return getOrNewFileSystem(authority).getPath(path)
    }

    private fun URI.requireSameScheme() {
        val scheme = scheme
        require(scheme == SCHEME) { "URI scheme $scheme must be $SCHEME" }
    }

    private val URI.smbAuthority: Authority
        get() {
            val port = if (port != -1) port else Authority.DEFAULT_PORT
            val userInfo = userInfo.orEmpty()
            val domainSeparatorIndex = userInfo.indexOf('\\')
            val username: String
            val domain: String?
            if (domainSeparatorIndex != -1) {
                username = userInfo.substring(domainSeparatorIndex + 1)
                domain = userInfo.substring(0, domainSeparatorIndex).takeIfNotEmpty()
            } else {
                username = userInfo
                domain = null
            }
            return Authority(host, port, username, domain)
        }

    @Throws(IOException::class)
    override fun newFileChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): FileChannel {
        file as? SmbPath ?: throw ProviderMismatchException(file.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun newByteChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): SeekableByteChannel {
        file as? SmbPath ?: throw ProviderMismatchException(file.toString())
        val openOptions = options.toOpenOptions()
        val desiredAccess = openOptions.toSmbDesiredAccess()
        val fileAttributes = openOptions.toSmbFileAttributes()
        val shareAccess = openOptions.toSmbShareAccess()
        val createDisposition = openOptions.toSmbCreateDisposition()
        val createOptions = openOptions.toSmbCreateOptions()
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(attributes.contentToString())
        }
        return try {
            Client.openByteChannel(
                file, desiredAccess, fileAttributes, shareAccess, createDisposition, createOptions,
                openOptions.append
            )
        } catch (e: ClientException) {
            throw e.toFileSystemException(file.toString())
        }
    }

    @Throws(IOException::class)
    override fun newDirectoryStream(
        directory: Path,
        filter: DirectoryStream.Filter<in Path>
    ): DirectoryStream<Path> {
        directory as? SmbPath ?: throw ProviderMismatchException(directory.toString())
        val iterator = try {
            @Suppress("UNCHECKED_CAST")
            Client.openDirectoryIterator(directory) as CloseableIterator<Path>
        } catch (e: ClientException) {
            throw e.toFileSystemException(directory.toString())
        }
        return PathIteratorDirectoryStream(iterator, iterator, filter)
    }

    @Throws(IOException::class)
    override fun createDirectory(directory: Path, vararg attributes: FileAttribute<*>) {
        directory as? SmbPath ?: throw ProviderMismatchException(directory.toString())
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(attributes.contentToString())
        }
        try {
            Client.createDirectory(directory)
        } catch (e: ClientException) {
            throw e.toFileSystemException(directory.toString())
        }
    }

    override fun createSymbolicLink(link: Path, target: Path, vararg attributes: FileAttribute<*>) {
        link as? SmbPath ?: throw ProviderMismatchException(link.toString())
        val targetString: String
        val isRelative: Boolean
        when (target) {
            is SmbPath -> {
                if(target.isAbsolute && target.authority.port != Authority.DEFAULT_PORT) {
                    throw InvalidFileNameException(
                        target.toString(), null, "Path is absolute but uses port ${
                        target.authority.port} instead of the default port ${
                        Authority.DEFAULT_PORT}"
                    )
                }
                targetString = target.toWindowsPath()
                isRelative = !target.isAbsolute
            }
            is ByteStringPath -> {
                targetString = target.toString()
                isRelative = true
            }
            else -> throw ProviderMismatchException(target.toString())
        }.toString()
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(attributes.contentToString())
        }
        val reparseData = SymbolicLinkReparseData(targetString, targetString, isRelative)
        try {
            Client.createSymbolicLink(link, reparseData)
        } catch (e: ClientException) {
            e.maybeThrowInvalidFileNameException(link.toString())
            throw e.toFileSystemException(link.toString(), targetString)
        }
    }

    override fun createLink(link: Path, existing: Path) {
        link as? SmbPath ?: throw ProviderMismatchException(link.toString())
        existing as? SmbPath ?: throw ProviderMismatchException(existing.toString())
        try {
            Client.createLink(existing, link, true)
        } catch (e: ClientException) {
            e.maybeThrowInvalidFileNameException(link.toString())
            throw e.toFileSystemException(link.toString(), existing.toString())
        }
    }

    @Throws(IOException::class)
    override fun delete(path: Path) {
        path as? SmbPath ?: throw ProviderMismatchException(path.toString())
        try {
            Client.delete(path)
        } catch (e: ClientException) {
            throw e.toFileSystemException(path.toString())
        }
    }

    override fun readSymbolicLink(link: Path): Path {
        link as? SmbPath ?: throw ProviderMismatchException(link.toString())
        val reparseData = try {
            Client.readSymbolicLink(link)
        } catch (e: ClientException) {
            throw e.toFileSystemException(link.toString())
        }
        val target = reparseData.substituteName
        return ByteStringPath(target.toByteString())
    }

    @Throws(IOException::class)
    override fun copy(source: Path, target: Path, vararg options: CopyOption) {
        source as? SmbPath ?: throw ProviderMismatchException(source.toString())
        target as? SmbPath ?: throw ProviderMismatchException(target.toString())
        val copyOptions = options.toCopyOptions()
        SmbCopyMove.copy(source, target, copyOptions)
    }

    @Throws(IOException::class)
    override fun move(source: Path, target: Path, vararg options: CopyOption) {
        source as? SmbPath ?: throw ProviderMismatchException(source.toString())
        target as? SmbPath ?: throw ProviderMismatchException(target.toString())
        val copyOptions = options.toCopyOptions()
        SmbCopyMove.move(source, target, copyOptions)
    }

    override fun isSameFile(path: Path, path2: Path): Boolean {
        path as? SmbPath ?: throw ProviderMismatchException(path.toString())
        if (path == path2) {
            return true
        }
        if (path2 !is SmbPath) {
            return false
        }
        if (path.authority != path2.authority) {
            return false
        }
        val sharePath = path.sharePath
        val sharePath2 = path2.sharePath
        if (sharePath == null || sharePath2 == null || sharePath.name != sharePath2.name
            || sharePath.path.isEmpty() || sharePath2.path.isEmpty()) {
            return false
        }
        val pathInformation = try {
            Client.getPathInformation(path, true)
        } catch (e: ClientException) {
            throw e.toFileSystemException(path.toString())
        } as FileInformation
        val path2Information = try {
            Client.getPathInformation(path2, true)
        } catch (e: ClientException) {
            throw e.toFileSystemException(path2.toString())
        } as FileInformation
        return (SmbFileKey(path, pathInformation.fileId)
            == SmbFileKey(path2, path2Information.fileId))
    }

    override fun isHidden(path: Path): Boolean {
        path as? SmbPath ?: throw ProviderMismatchException(path.toString())
        val fileName = path.fileNameByteString ?: return false
        return fileName.startsWith(HIDDEN_FILE_NAME_PREFIX)
    }

    override fun getFileStore(path: Path): FileStore {
        path as? SmbPath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun checkAccess(path: Path, vararg modes: AccessMode) {
        path as? SmbPath ?: throw ProviderMismatchException(path.toString())
        val accessModes = modes.toAccessModes()
        val desiredAccess = enumSetOf<AccessMask>()
        if (accessModes.read) {
            desiredAccess += AccessMask.GENERIC_READ
        }
        if (accessModes.write) {
            desiredAccess += AccessMask.GENERIC_WRITE
        }
        if (accessModes.execute) {
            desiredAccess += AccessMask.GENERIC_EXECUTE
        }
        try {
            Client.checkAccess(path, desiredAccess, false)
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
        return getFileAttributeView(path, *options) as V
    }

    internal fun supportsFileAttributeView(type: Class<out FileAttributeView>): Boolean =
        type.isAssignableFrom(SmbFileAttributeView::class.java)

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

    private fun getFileAttributeView(path: Path, vararg options: LinkOption): SmbFileAttributeView {
        path as? SmbPath ?: throw ProviderMismatchException(path.toString())
        val linkOptions = options.toLinkOptions()
        return SmbFileAttributeView(path, linkOptions.noFollowLinks)
    }

    override fun readAttributes(
        path: Path,
        attributes: String,
        vararg options: LinkOption
    ): Map<String, Any> {
        path as? SmbPath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    override fun setAttribute(
        path: Path,
        attribute: String,
        value: Any,
        vararg options: LinkOption
    ) {
        path as? SmbPath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun observe(path: Path, intervalMillis: Long): PathObservable {
        path as? SmbPath ?: throw ProviderMismatchException(path.toString())
        return WatchServicePathObservable(path, intervalMillis)
    }

    @Throws(IOException::class)
    override fun search(
        directory: Path,
        query: String,
        intervalMillis: Long,
        listener: (List<Path>) -> Unit
    ) {
        directory as? SmbPath ?: throw ProviderMismatchException(directory.toString())
        WalkFileTreeSearchable.search(directory, query, intervalMillis, listener)
    }
}
