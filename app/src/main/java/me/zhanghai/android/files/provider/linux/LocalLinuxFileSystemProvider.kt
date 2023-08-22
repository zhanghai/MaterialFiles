/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux

import android.system.OsConstants
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
import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.attribute.FileAttribute
import java8.nio.file.attribute.FileAttributeView
import java8.nio.file.spi.FileSystemProvider
import me.zhanghai.android.files.provider.common.ByteStringPath
import me.zhanghai.android.files.provider.common.PathObservable
import me.zhanghai.android.files.provider.common.PathObservableProvider
import me.zhanghai.android.files.provider.common.PosixFileMode
import me.zhanghai.android.files.provider.common.Searchable
import me.zhanghai.android.files.provider.common.WalkFileTreeSearchable
import me.zhanghai.android.files.provider.common.WatchServicePathObservable
import me.zhanghai.android.files.provider.common.decodedPathByteString
import me.zhanghai.android.files.provider.common.open
import me.zhanghai.android.files.provider.common.toAccessModes
import me.zhanghai.android.files.provider.common.toByteString
import me.zhanghai.android.files.provider.common.toCopyOptions
import me.zhanghai.android.files.provider.common.toInt
import me.zhanghai.android.files.provider.common.toLinkOptions
import me.zhanghai.android.files.provider.common.toOpenOptions
import me.zhanghai.android.files.provider.linux.media.MediaScanner
import me.zhanghai.android.files.provider.linux.syscall.SyscallException
import me.zhanghai.android.files.provider.linux.syscall.Syscall
import me.zhanghai.android.files.util.hasBits
import java.io.IOException
import java.net.URI

class LocalLinuxFileSystemProvider(provider: LinuxFileSystemProvider) : FileSystemProvider(),
    PathObservableProvider, Searchable {
    internal val fileSystem: LinuxFileSystem = LinuxFileSystem(provider)

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
        val path = uri.decodedPathByteString
            ?: throw IllegalArgumentException("URI must have a path")
        return fileSystem.getPath(path)
    }

    private fun URI.requireSameScheme() {
        val scheme = scheme
        require(scheme == SCHEME) { "URI scheme $scheme must be $SCHEME" }
    }

    @Throws(IOException::class)
    override fun newFileChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): FileChannel {
        file as? LinuxPath ?: throw ProviderMismatchException(file.toString())
        val fileBytes = file.toByteString()
        val openOptions = options.toOpenOptions()
        val flags = openOptions.toLinuxFlags()
        val mode = (PosixFileMode.fromAttributes(attributes) ?: PosixFileMode.CREATE_FILE_DEFAULT)
            .toInt()
        val fd = try {
            Syscall.open(fileBytes, flags, mode)
        } catch (e: SyscallException) {
            if (flags.hasBits(OsConstants.O_CREAT)) {
                e.maybeThrowInvalidFileNameException(fileBytes.toString())
            }
            throw e.toFileSystemException(fileBytes.toString())
        }
        val fileChannel = FileChannel::class.open(fd, flags)
        if (openOptions.deleteOnClose) {
            try {
                Syscall.remove(fileBytes)
            } catch (e: SyscallException) {
                e.printStackTrace()
            }
        }
        val javaFile = file.toFile()
        MediaScanner.scan(javaFile)
        return MediaScanner.createScanOnCloseFileChannel(fileChannel, javaFile)
    }

    @Throws(IOException::class)
    override fun newByteChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): SeekableByteChannel = newFileChannel(file, options, *attributes)

    @Throws(IOException::class)
    override fun newDirectoryStream(
        directory: Path,
        filter: DirectoryStream.Filter<in Path>
    ): DirectoryStream<Path> {
        directory as? LinuxPath ?: throw ProviderMismatchException(directory.toString())
        val directoryBytes = directory.toByteString()
        val dir = try {
            Syscall.opendir(directoryBytes)
        } catch (e: SyscallException) {
            throw e.toFileSystemException(directoryBytes.toString())
        }
        return LinuxDirectoryStream(directory, dir, filter)
    }

    @Throws(IOException::class)
    override fun createDirectory(directory: Path, vararg attributes: FileAttribute<*>) {
        directory as? LinuxPath ?: throw ProviderMismatchException(directory.toString())
        val directoryBytes = directory.toByteString()
        val mode = (PosixFileMode.fromAttributes(attributes)
            ?: PosixFileMode.CREATE_DIRECTORY_DEFAULT).toInt()
        try {
            Syscall.mkdir(directoryBytes, mode)
        } catch (e: SyscallException) {
            e.maybeThrowInvalidFileNameException(directoryBytes.toString())
            throw e.toFileSystemException(directoryBytes.toString())
        }
        MediaScanner.scan(directory.toFile())
    }

    @Throws(IOException::class)
    override fun createSymbolicLink(link: Path, target: Path, vararg attributes: FileAttribute<*>) {
        link as? LinuxPath ?: throw ProviderMismatchException(link.toString())
        val targetBytes = when (target) {
            is LinuxPath -> target.toByteString()
            is ByteStringPath -> target.toByteString()
            else -> throw ProviderMismatchException(target.toString())
        }
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(attributes.contentToString())
        }
        val linkBytes = link.toByteString()
        try {
            Syscall.symlink(targetBytes, linkBytes)
        } catch (e: SyscallException) {
            e.maybeThrowInvalidFileNameException(linkBytes.toString())
            throw e.toFileSystemException(linkBytes.toString(), targetBytes.toString())
        }
        MediaScanner.scan(link.toFile())
    }

    @Throws(IOException::class)
    override fun createLink(link: Path, existing: Path) {
        link as? LinuxPath ?: throw ProviderMismatchException(link.toString())
        existing as? LinuxPath ?: throw ProviderMismatchException(existing.toString())
        val oldPathBytes = existing.toByteString()
        val newPathBytes = link.toByteString()
        try {
            Syscall.link(oldPathBytes, newPathBytes)
        } catch (e: SyscallException) {
            e.maybeThrowInvalidFileNameException(newPathBytes.toString())
            throw e.toFileSystemException(newPathBytes.toString(), oldPathBytes.toString())
        }
        MediaScanner.scan(link.toFile())
    }

    @Throws(IOException::class)
    override fun delete(path: Path) {
        path as? LinuxPath ?: throw ProviderMismatchException(path.toString())
        val pathBytes = path.toByteString()
        try {
            Syscall.remove(pathBytes)
        } catch (e: SyscallException) {
            throw e.toFileSystemException(pathBytes.toString())
        }
        MediaScanner.scan(path.toFile(), true)
    }

    @Throws(IOException::class)
    override fun readSymbolicLink(link: Path): Path {
        link as? LinuxPath ?: throw ProviderMismatchException(link.toString())
        val linkBytes = link.toByteString()
        val targetBytes = try {
            Syscall.readlink(linkBytes)
        } catch (e: SyscallException) {
            e.maybeThrowNotLinkException(linkBytes.toString())
            throw e.toFileSystemException(linkBytes.toString())
        }
        return ByteStringPath(targetBytes)
    }

    @Throws(IOException::class)
    override fun copy(source: Path, target: Path, vararg options: CopyOption) {
        source as? LinuxPath ?: throw ProviderMismatchException(source.toString())
        target as? LinuxPath ?: throw ProviderMismatchException(target.toString())
        val sourceBytes = source.toByteString()
        val targetBytes = target.toByteString()
        val copyOptions = options.toCopyOptions()
        LinuxCopyMove.copy(sourceBytes, targetBytes, copyOptions)
        MediaScanner.scan(target.toFile())
    }

    @Throws(IOException::class)
    override fun move(source: Path, target: Path, vararg options: CopyOption) {
        source as? LinuxPath ?: throw ProviderMismatchException(source.toString())
        target as? LinuxPath ?: throw ProviderMismatchException(target.toString())
        val sourceBytes = source.toByteString()
        val targetBytes = target.toByteString()
        val copyOptions = options.toCopyOptions()
        LinuxCopyMove.move(sourceBytes, targetBytes, copyOptions)
        MediaScanner.scan(source.toFile())
        MediaScanner.scan(target.toFile())
    }

    @Throws(IOException::class)
    override fun isSameFile(path: Path, path2: Path): Boolean {
        path as? LinuxPath ?: throw ProviderMismatchException(path.toString())
        if (path == path2) {
            return true
        }
        if (path2 !is LinuxPath) {
            return false
        }
        path2 as? LinuxPath ?: throw ProviderMismatchException(path2.toString())
        val pathBytes = path.toByteString()
        val path2Bytes = path2.toByteString()
        val pathStat = try {
            Syscall.lstat(pathBytes)
        } catch (e: SyscallException) {
            throw e.toFileSystemException(pathBytes.toString())
        }
        val path2Stat = try {
            Syscall.lstat(path2Bytes)
        } catch (e: SyscallException) {
            throw e.toFileSystemException(path2Bytes.toString())
        }
        return pathStat.st_dev == path2Stat.st_dev && pathStat.st_ino == path2Stat.st_ino
    }

    override fun isHidden(path: Path): Boolean {
        path as? LinuxPath ?: throw ProviderMismatchException(path.toString())
        val fileName = path.fileName ?: return false
        val fileNameBytes = fileName.toByteString()
        return fileNameBytes.startsWith(HIDDEN_FILE_NAME_PREFIX)
    }

    @Throws(IOException::class)
    override fun getFileStore(path: Path): FileStore {
        path as? LinuxPath ?: throw ProviderMismatchException(path.toString())
        return LinuxFileStore(path)
    }

    @Throws(IOException::class)
    override fun checkAccess(path: Path, vararg modes: AccessMode) {
        path as? LinuxPath ?: throw ProviderMismatchException(path.toString())
        val pathBytes = path.toByteString()
        val accessModes = modes.toAccessModes()
        var mode: Int
        if (!(accessModes.read || accessModes.write || accessModes.execute)) {
            mode = OsConstants.F_OK
        } else {
            mode = 0
            if (accessModes.read) {
                mode = mode or OsConstants.R_OK
            }
            if (accessModes.write) {
                mode = mode or OsConstants.W_OK
            }
            if (accessModes.execute) {
                mode = mode or OsConstants.X_OK
            }
        }
        val accessible = try {
            // TODO: Should use euidaccess() but that's unavailable on Android.
            Syscall.access(pathBytes, mode)
        } catch (e: SyscallException) {
            throw e.toFileSystemException(pathBytes.toString())
        }
        if (!accessible) {
            throw AccessDeniedException(pathBytes.toString())
        }
    }

    override fun <V : FileAttributeView> getFileAttributeView(
        path: Path,
        type: Class<V>,
        vararg options: LinkOption
    ): V? {
        path as? LinuxPath ?: throw ProviderMismatchException(path.toString())
        if (!supportsFileAttributeView(type)) {
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return getFileAttributeView(path, *options) as V
    }

    @Throws(IOException::class)
    override fun <A : BasicFileAttributes> readAttributes(
        path: Path,
        type: Class<A>,
        vararg options: LinkOption
    ): A {
        path as? LinuxPath ?: throw ProviderMismatchException(path.toString())
        if (!type.isAssignableFrom(LinuxFileAttributes::class.java)) {
            throw UnsupportedOperationException(type.toString())
        }
        @Suppress("UNCHECKED_CAST")
        return getFileAttributeView(path, *options).readAttributes() as A
    }

    private fun getFileAttributeView(
        path: Path,
        vararg options: LinkOption
    ): LinuxFileAttributeView {
        path as? LinuxPath ?: throw ProviderMismatchException(path.toString())
        val linkOptions = options.toLinkOptions()
        return LinuxFileAttributeView(path, linkOptions.noFollowLinks)
    }

    override fun readAttributes(
        path: Path,
        attributes: String,
        vararg options: LinkOption
    ): Map<String, Any> {
        path as? LinuxPath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    override fun setAttribute(
        path: Path,
        attribute: String,
        value: Any,
        vararg options: LinkOption
    ) {
        path as? LinuxPath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun observe(path: Path, intervalMillis: Long): PathObservable {
        path as? LinuxPath ?: throw ProviderMismatchException(path.toString())
        return WatchServicePathObservable(path, intervalMillis)
    }

    @Throws(IOException::class)
    override fun search(
        directory: Path,
        query: String,
        intervalMillis: Long,
        listener: (List<Path>) -> Unit
    ) {
        directory as? LinuxPath ?: throw ProviderMismatchException(directory.toString())
        WalkFileTreeSearchable.search(directory, query, intervalMillis, listener)
    }

    companion object {
        internal const val SCHEME = "file"

        private val HIDDEN_FILE_NAME_PREFIX = ".".toByteString()

        internal fun supportsFileAttributeView(type: Class<out FileAttributeView>): Boolean =
            type.isAssignableFrom(LinuxFileAttributeView::class.java)
    }
}
