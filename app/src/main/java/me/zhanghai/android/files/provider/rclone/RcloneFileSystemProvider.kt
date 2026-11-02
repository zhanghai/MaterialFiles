/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.rclone

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
import java8.nio.file.NoSuchFileException
import java8.nio.file.OpenOption
import java8.nio.file.Path
import java8.nio.file.ProviderMismatchException
import java8.nio.file.StandardOpenOption
import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.attribute.FileAttribute
import java8.nio.file.attribute.FileAttributeView
import java8.nio.file.spi.FileSystemProvider
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.ForeignCopyMove
import me.zhanghai.android.files.provider.common.PathListDirectoryStream
import me.zhanghai.android.files.provider.common.PathObservable
import me.zhanghai.android.files.provider.common.PathObservableProvider
import me.zhanghai.android.files.provider.common.Searchable
import me.zhanghai.android.files.provider.common.WalkFileTreeSearchable
import me.zhanghai.android.files.provider.common.WatchServicePathObservable
import me.zhanghai.android.files.provider.common.decodedPathByteString
import me.zhanghai.android.files.provider.common.toAccessModes
import me.zhanghai.android.files.provider.common.toCopyOptions
import me.zhanghai.android.files.provider.common.toOpenOptions
import me.zhanghai.android.files.provider.common.toByteString
import me.zhanghai.android.files.provider.rclone.client.RcloneClient
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URI

object RcloneFileSystemProvider : FileSystemProvider(), PathObservableProvider, Searchable {
    const val SCHEME = "rclone"
    private val HIDDEN_FILE_NAME_PREFIX = ".".toByteString()

    private val fileSystems = mutableMapOf<RcloneAuthority, RcloneFileSystem>()
    private val lock = Any()

    override fun getScheme(): String = SCHEME

    override fun newFileSystem(uri: URI, env: Map<String, *>): FileSystem {
        uri.requireSameScheme()
        val authority = uri.rcloneAuthority
        synchronized(lock) {
            if (fileSystems[authority] != null) {
                throw FileSystemAlreadyExistsException(authority.toString())
            }
            return newFileSystemLocked(authority)
        }
    }

    internal fun getOrNewFileSystem(authority: RcloneAuthority): RcloneFileSystem =
        synchronized(lock) { fileSystems[authority] ?: newFileSystemLocked(authority) }

    private fun newFileSystemLocked(authority: RcloneAuthority): RcloneFileSystem {
        val fileSystem = RcloneFileSystem(this, authority)
        fileSystems[authority] = fileSystem
        return fileSystem
    }

    override fun getFileSystem(uri: URI): FileSystem {
        uri.requireSameScheme()
        val authority = uri.rcloneAuthority
        return synchronized(lock) { fileSystems[authority] }
            ?: throw FileSystemNotFoundException(authority.toString())
    }

    internal fun removeFileSystem(fileSystem: RcloneFileSystem) {
        val authority = fileSystem.authority
        synchronized(lock) { fileSystems.remove(authority) }
    }

    override fun getPath(uri: URI): Path {
        uri.requireSameScheme()
        val authority = uri.rcloneAuthority
        val path = uri.decodedPathByteString
            ?: throw IllegalArgumentException("URI must have a path")
        return getOrNewFileSystem(authority).getPath(path)
    }

    private fun URI.requireSameScheme() {
        require(scheme.equals(SCHEME, ignoreCase = true)) {
            "URI scheme $scheme must be $SCHEME"
        }
    }

    private val URI.rcloneAuthority: RcloneAuthority
        get() {
            val remoteName = host?.takeIf { it.isNotEmpty() }
                ?: authority?.takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException("URI must have host or authority: $this")
            return RcloneAuthority(remoteName)
        }

    @Throws(IOException::class)
    override fun newInputStream(file: Path, vararg options: OpenOption): InputStream {
        file as? RclonePath ?: throw ProviderMismatchException(file.toString())
        val openOptions = options.toOpenOptions()
        if (openOptions.write) {
            throw UnsupportedOperationException(StandardOpenOption.WRITE.toString())
        }
        if (openOptions.append) {
            throw UnsupportedOperationException(StandardOpenOption.APPEND.toString())
        }
        return RcloneClient.download(file.remoteName, file.remotePath)
    }

    @Throws(IOException::class)
    override fun newOutputStream(file: Path, vararg options: OpenOption): OutputStream {
        file as? RclonePath ?: throw ProviderMismatchException(file.toString())
        val tempFile = File.createTempFile("rclone_out", null)
        val fileOutputStream = FileOutputStream(tempFile)
        return object : OutputStream() {
            override fun write(b: Int) {
                fileOutputStream.write(b)
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                fileOutputStream.write(b, off, len)
            }

            override fun flush() {
                fileOutputStream.flush()
            }

            override fun close() {
                try {
                    fileOutputStream.close()
                    RcloneClient.uploadFile(file.remoteName, file.remotePath, tempFile)
                } finally {
                    tempFile.delete()
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun newFileChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): FileChannel {
        file as? RclonePath ?: throw ProviderMismatchException(file.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun newByteChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): SeekableByteChannel {
        file as? RclonePath ?: throw ProviderMismatchException(file.toString())
        val openOptions = options.toOpenOptions()
        return RcloneFileByteChannel(file, openOptions.append)
    }

    @Throws(IOException::class)
    override fun newDirectoryStream(
        directory: Path,
        filter: DirectoryStream.Filter<in Path>
    ): DirectoryStream<Path> {
        directory as? RclonePath ?: throw ProviderMismatchException(directory.toString())
        val items = RcloneClient.list(directory.remoteName, directory.remotePath)
        val paths = items.map { directory.resolve(it.name) }
        return PathListDirectoryStream(paths, filter)
    }

    @Throws(IOException::class)
    override fun createDirectory(directory: Path, vararg attributes: FileAttribute<*>) {
        directory as? RclonePath ?: throw ProviderMismatchException(directory.toString())
        RcloneClient.mkdir(directory.remoteName, directory.remotePath)
    }

    override fun createSymbolicLink(link: Path, target: Path, vararg attributes: FileAttribute<*>) {
        throw UnsupportedOperationException()
    }

    override fun createLink(link: Path, existing: Path) {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun delete(path: Path) {
        path as? RclonePath ?: throw ProviderMismatchException(path.toString())
        val attributes = try {
            readAttributes(path, BasicFileAttributes::class.java)
        } catch (e: NoSuchFileException) {
            throw e
        }
        if (attributes.isDirectory) {
            RcloneClient.purge(path.remoteName, path.remotePath)
        } else {
            RcloneClient.deleteFile(path.remoteName, path.remotePath)
        }
    }

    override fun readSymbolicLink(link: Path): Path {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun copy(source: Path, target: Path, vararg options: CopyOption) {
        if (source is RclonePath && target is RclonePath) {
            RcloneCopyMove.copy(source, target, options.toCopyOptions())
        } else {
            ForeignCopyMove.copy(source, target, *options)
        }
    }

    @Throws(IOException::class)
    override fun move(source: Path, target: Path, vararg options: CopyOption) {
        if (source is RclonePath && target is RclonePath) {
            RcloneCopyMove.move(source, target, options.toCopyOptions())
        } else {
            ForeignCopyMove.move(source, target, *options)
        }
    }

    override fun isSameFile(path: Path, path2: Path): Boolean {
        path as? RclonePath ?: throw ProviderMismatchException(path.toString())
        return path == path2
    }

    override fun isHidden(path: Path): Boolean {
        path as? RclonePath ?: throw ProviderMismatchException(path.toString())
        val fileName = path.fileNameByteString ?: return false
        return fileName.startsWith(HIDDEN_FILE_NAME_PREFIX)
    }

    override fun getFileStore(path: Path): FileStore {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun checkAccess(path: Path, vararg modes: AccessMode) {
        path as? RclonePath ?: throw ProviderMismatchException(path.toString())
        val accessModes = modes.toAccessModes()
        if (accessModes.execute) {
            throw UnsupportedOperationException(AccessMode.EXECUTE.toString())
        }
        RcloneClient.stat(path.remoteName, path.remotePath)
            ?: throw NoSuchFileException(path.toString())
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
        type.isAssignableFrom(RcloneFileAttributeView::class.java)

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

    private fun getFileAttributeView(path: Path, vararg options: LinkOption): RcloneFileAttributeView {
        path as? RclonePath ?: throw ProviderMismatchException(path.toString())
        return RcloneFileAttributeView(path)
    }

    override fun readAttributes(
        path: Path,
        attributes: String,
        vararg options: LinkOption
    ): Map<String, Any> {
        throw UnsupportedOperationException()
    }

    override fun setAttribute(
        path: Path,
        attribute: String,
        value: Any,
        vararg options: LinkOption
    ) {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun observe(path: Path, intervalMillis: Long): PathObservable {
        path as? RclonePath ?: throw ProviderMismatchException(path.toString())
        return WatchServicePathObservable(path, intervalMillis)
    }

    @Throws(IOException::class)
    override fun search(
        directory: Path,
        query: String,
        intervalMillis: Long,
        listener: (List<Path>) -> Unit
    ) {
        directory as? RclonePath ?: throw ProviderMismatchException(directory.toString())
        WalkFileTreeSearchable.search(directory, query, intervalMillis, listener)
    }
}
