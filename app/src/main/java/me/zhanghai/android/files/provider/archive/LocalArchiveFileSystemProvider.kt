/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive

import java8.nio.channels.FileChannel
import java8.nio.channels.SeekableByteChannel
import java8.nio.file.AccessDeniedException
import java8.nio.file.AccessMode
import java8.nio.file.CopyOption
import java8.nio.file.DirectoryStream
import java8.nio.file.FileStore
import java8.nio.file.FileSystem
import java8.nio.file.LinkOption
import java8.nio.file.OpenOption
import java8.nio.file.Path
import java8.nio.file.Paths
import java8.nio.file.ProviderMismatchException
import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.attribute.FileAttribute
import java8.nio.file.attribute.FileAttributeView
import java8.nio.file.spi.FileSystemProvider
import me.zhanghai.android.files.provider.common.ByteStringPath
import me.zhanghai.android.files.provider.common.FileSystemCache
import me.zhanghai.android.files.provider.common.PathListDirectoryStream
import me.zhanghai.android.files.provider.common.ReadOnlyFileSystemException
import me.zhanghai.android.files.provider.common.Searchable
import me.zhanghai.android.files.provider.common.WalkFileTreeSearchable
import me.zhanghai.android.files.provider.common.decodedFragmentByteString
import me.zhanghai.android.files.provider.common.decodedSchemeSpecificPartByteString
import me.zhanghai.android.files.provider.common.isSameFile
import me.zhanghai.android.files.provider.common.toAccessModes
import me.zhanghai.android.files.provider.common.toByteString
import me.zhanghai.android.files.provider.common.toOpenOptions
import java.io.IOException
import java.io.InputStream
import java.net.URI

class LocalArchiveFileSystemProvider(
    private val provider: ArchiveFileSystemProvider
) : FileSystemProvider(), Searchable {
    private val fileSystems = FileSystemCache<Path, ArchiveFileSystem>()

    override fun getScheme(): String = SCHEME

    override fun newFileSystem(uri: URI, env: Map<String, *>): FileSystem {
        uri.requireSameScheme()
        val archiveFile = uri.archiveFile
        return fileSystems.create(archiveFile) { newFileSystem(archiveFile) }
    }

    override fun newFileSystem(file: Path, env: Map<String, *>): FileSystem = newFileSystem(file)

    internal fun getOrNewFileSystem(archiveFile: Path): ArchiveFileSystem =
        fileSystems.getOrCreate(archiveFile) { newFileSystem(archiveFile) }

    private fun newFileSystem(archiveFile: Path): ArchiveFileSystem =
        ArchiveFileSystem(provider, archiveFile)

    override fun getFileSystem(uri: URI): FileSystem {
        uri.requireSameScheme()
        val archiveFile = uri.archiveFile
        return fileSystems[archiveFile]
    }

    internal fun removeFileSystem(fileSystem: ArchiveFileSystem) {
        fileSystems.remove(fileSystem.archiveFile, fileSystem)
    }

    override fun getPath(uri: URI): Path {
        uri.requireSameScheme()
        val archiveFile = uri.archiveFile
        val fragment = uri.decodedFragmentByteString
            ?: throw IllegalArgumentException("URI must have a fragment")
        return getOrNewFileSystem(archiveFile).getPath(fragment)
    }

    private fun URI.requireSameScheme() {
        val scheme = scheme
        require(scheme == SCHEME) { "URI scheme $scheme must be $SCHEME" }
    }

    private val URI.archiveFile: Path
        get() {
            val schemeSpecificPart = decodedSchemeSpecificPartByteString
                ?: throw IllegalArgumentException("URI must have a scheme specific part")
            val archiveUri = URI.create(schemeSpecificPart.toString())
            return Paths.get(archiveUri)
        }

    @Throws(IOException::class)
    override fun newInputStream(file: Path, vararg options: OpenOption): InputStream {
        file as? ArchivePath ?: throw ProviderMismatchException(file.toString())
        options.toOpenOptions().checkForArchive()
        return file.fileSystem.newInputStreamAsLocal(file)
    }

    override fun newFileChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): FileChannel {
        file as? ArchivePath ?: throw ProviderMismatchException(file.toString())
        options.toOpenOptions().checkForArchive()
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(attributes.contentToString())
        }
        throw UnsupportedOperationException()
    }

    override fun newByteChannel(
        file: Path,
        options: Set<OpenOption>,
        vararg attributes: FileAttribute<*>
    ): SeekableByteChannel {
        file as? ArchivePath ?: throw ProviderMismatchException(file.toString())
        options.toOpenOptions().checkForArchive()
        if (attributes.isNotEmpty()) {
            throw UnsupportedOperationException(attributes.contentToString())
        }
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun newDirectoryStream(
        directory: Path,
        filter: DirectoryStream.Filter<in Path>
    ): DirectoryStream<Path> {
        directory as? ArchivePath ?: throw ProviderMismatchException(directory.toString())
        val children = directory.fileSystem.getDirectoryChildrenAsLocal(directory)
        return PathListDirectoryStream(children, filter)
    }

    @Throws(IOException::class)
    override fun createDirectory(directory: Path, vararg attributes: FileAttribute<*>) {
        directory as? ArchivePath ?: throw ProviderMismatchException(directory.toString())
        throw ReadOnlyFileSystemException(directory.toString())
    }

    @Throws(IOException::class)
    override fun createSymbolicLink(link: Path, target: Path, vararg attributes: FileAttribute<*>) {
        link as? ArchivePath ?: throw ProviderMismatchException(link.toString())
        when (target) {
            is ArchivePath, is ByteStringPath -> {}
            else -> throw ProviderMismatchException(target.toString())
        }
        throw ReadOnlyFileSystemException(link.toString(), target.toString(), null)
    }

    @Throws(IOException::class)
    override fun createLink(link: Path, existing: Path) {
        link as? ArchivePath ?: throw ProviderMismatchException(link.toString())
        existing as? ArchivePath ?: throw ProviderMismatchException(existing.toString())
        throw ReadOnlyFileSystemException(link.toString(), existing.toString(), null)
    }

    @Throws(IOException::class)
    override fun delete(path: Path) {
        path as? ArchivePath ?: throw ProviderMismatchException(path.toString())
        throw ReadOnlyFileSystemException(path.toString())
    }

    @Throws(IOException::class)
    override fun readSymbolicLink(link: Path): Path {
        link as? ArchivePath ?: throw ProviderMismatchException(link.toString())
        val target = link.fileSystem.readSymbolicLinkAsLocal(link)
        return ByteStringPath(target.toByteString())
    }

    @Throws(IOException::class)
    override fun copy(source: Path, target: Path, vararg options: CopyOption) {
        source as? ArchivePath ?: throw ProviderMismatchException(source.toString())
        target as? ArchivePath ?: throw ProviderMismatchException(target.toString())
        throw ReadOnlyFileSystemException(source.toString(), target.toString(), null)
    }

    @Throws(IOException::class)
    override fun move(source: Path, target: Path, vararg options: CopyOption) {
        source as? ArchivePath ?: throw ProviderMismatchException(source.toString())
        target as? ArchivePath ?: throw ProviderMismatchException(target.toString())
        throw ReadOnlyFileSystemException(source.toString(), target.toString(), null)
    }

    @Throws(IOException::class)
    override fun isSameFile(path: Path, path2: Path): Boolean {
        path as? ArchivePath ?: throw ProviderMismatchException(path.toString())
        if (path == path2) {
            return true
        }
        if (path2 !is ArchivePath) {
            return false
        }
        val fileSystem = path.fileSystem
        if (!fileSystem.archiveFile.isSameFile(path2.fileSystem.archiveFile)) {
            return false
        }
        return path == fileSystem.getPath(path2.toString())
    }

    override fun isHidden(path: Path): Boolean {
        path as? ArchivePath ?: throw ProviderMismatchException(path.toString())
        return false
    }

    override fun getFileStore(path: Path): FileStore {
        path as? ArchivePath ?: throw ProviderMismatchException(path.toString())
        val archiveFile = path.fileSystem.archiveFile
        return ArchiveFileStore(archiveFile)
    }

    @Throws(IOException::class)
    override fun checkAccess(path: Path, vararg modes: AccessMode) {
        path as? ArchivePath ?: throw ProviderMismatchException(path.toString())
        val accessModes = modes.toAccessModes()
        path.fileSystem.getEntryAsLocal(path)
        if (accessModes.write || accessModes.execute) {
            throw AccessDeniedException(path.toString())
        }
    }

    override fun <V : FileAttributeView> getFileAttributeView(
        path: Path,
        type: Class<V>,
        vararg options: LinkOption
    ): V? {
        path as? ArchivePath ?: throw ProviderMismatchException(path.toString())
        if (!supportsFileAttributeView(type)) {
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return getFileAttributeView(path) as V
    }

    @Throws(IOException::class)
    override fun <A : BasicFileAttributes> readAttributes(
        path: Path,
        type: Class<A>,
        vararg options: LinkOption
    ): A {
        path as? ArchivePath ?: throw ProviderMismatchException(path.toString())
        if (!type.isAssignableFrom(ArchiveFileAttributes::class.java)) {
            throw UnsupportedOperationException(type.toString())
        }
        @Suppress("UNCHECKED_CAST")
        return getFileAttributeView(path).readAttributes() as A
    }

    private fun getFileAttributeView(path: ArchivePath): ArchiveFileAttributeView =
        ArchiveFileAttributeView(path)

    override fun readAttributes(
        path: Path,
        attributes: String,
        vararg options: LinkOption
    ): Map<String, Any> {
        path as? ArchivePath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    override fun setAttribute(
        path: Path,
        attribute: String,
        value: Any,
        vararg options: LinkOption
    ) {
        path as? ArchivePath ?: throw ProviderMismatchException(path.toString())
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun search(
        directory: Path,
        query: String,
        intervalMillis: Long,
        listener: (List<Path>) -> Unit
    ) {
        directory as? ArchivePath ?: throw ProviderMismatchException(directory.toString())
        WalkFileTreeSearchable.search(directory, query, intervalMillis, listener)
    }

    companion object {
        internal const val SCHEME = "archive"

        internal fun supportsFileAttributeView(type: Class<out FileAttributeView>): Boolean =
            type.isAssignableFrom(ArchiveFileAttributeView::class.java)
    }
}
