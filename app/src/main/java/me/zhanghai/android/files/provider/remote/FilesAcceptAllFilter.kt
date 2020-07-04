/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote

import java8.nio.channels.SeekableByteChannel
import java8.nio.file.AccessMode
import java8.nio.file.CopyOption
import java8.nio.file.DirectoryStream
import java8.nio.file.FileStore
import java8.nio.file.FileSystem
import java8.nio.file.Files
import java8.nio.file.LinkOption
import java8.nio.file.OpenOption
import java8.nio.file.Path
import java8.nio.file.PathMatcher
import java8.nio.file.WatchEvent
import java8.nio.file.WatchKey
import java8.nio.file.WatchService
import java8.nio.file.attribute.BasicFileAttributes
import java8.nio.file.attribute.FileAttribute
import java8.nio.file.attribute.FileAttributeView
import java8.nio.file.attribute.UserPrincipalLookupService
import java8.nio.file.spi.FileSystemProvider
import java.io.File
import java.net.URI

val filesAcceptAllFilter: DirectoryStream.Filter<in Path> = run {
    var capturedFilter: DirectoryStream.Filter<in Path>? = null
    val path = object : StubPath() {
        override fun getFileSystem(): FileSystem =
            object : StubFileSystem() {
                override fun provider(): FileSystemProvider =
                    object : StubFileSystemProvider() {
                        override fun newDirectoryStream(
                            dir: Path,
                            filter: DirectoryStream.Filter<in Path>
                        ): DirectoryStream<Path> {
                            capturedFilter = filter
                            return StubDirectoryStream()
                        }
                    }
            }
    }
    Files.newDirectoryStream(path)
    capturedFilter!!
}

private open class StubPath : Path {
    override fun toFile(): File = throw AssertionError()

    override fun isAbsolute(): Boolean = throw AssertionError()

    override fun getFileName(): Path = throw AssertionError()

    override fun getName(index: Int): Path = throw AssertionError()

    override fun subpath(beginIndex: Int, endIndex: Int): Path = throw AssertionError()

    override fun endsWith(other: Path): Boolean = throw AssertionError()

    override fun endsWith(other: String): Boolean = throw AssertionError()

    override fun register(
        watcher: WatchService,
        events: Array<out WatchEvent.Kind<*>>,
        vararg modifiers: WatchEvent.Modifier
    ): WatchKey = throw AssertionError()

    override fun register(watcher: WatchService, vararg events: WatchEvent.Kind<*>): WatchKey =
        throw AssertionError()

    override fun iterator(): MutableIterator<Path> = throw AssertionError()

    override fun relativize(other: Path): Path = throw AssertionError()

    override fun toUri(): URI = throw AssertionError()

    override fun toRealPath(vararg options: LinkOption): Path = throw AssertionError()

    override fun normalize(): Path = throw AssertionError()

    override fun getParent(): Path = throw AssertionError()

    override fun compareTo(other: Path): Int = throw AssertionError()

    override fun getNameCount(): Int = throw AssertionError()

    override fun startsWith(other: Path): Boolean = throw AssertionError()

    override fun startsWith(other: String): Boolean = throw AssertionError()

    override fun getFileSystem(): FileSystem = throw AssertionError()

    override fun getRoot(): Path = throw AssertionError()

    override fun resolveSibling(other: Path): Path = throw AssertionError()

    override fun resolveSibling(other: String): Path = throw AssertionError()

    override fun resolve(other: Path): Path = throw AssertionError()

    override fun resolve(other: String): Path = throw AssertionError()

    override fun toAbsolutePath(): Path = throw AssertionError()
}

private open class StubFileSystem : FileSystem() {
    override fun getSeparator(): String = throw AssertionError()

    override fun newWatchService(): WatchService = throw AssertionError()

    override fun supportedFileAttributeViews(): MutableSet<String> = throw AssertionError()

    override fun isReadOnly(): Boolean = throw AssertionError()

    override fun getFileStores(): MutableIterable<FileStore> = throw AssertionError()

    override fun getPath(first: String, vararg more: String): Path = throw AssertionError()

    override fun provider(): FileSystemProvider = throw AssertionError()

    override fun isOpen(): Boolean = throw AssertionError()

    override fun getUserPrincipalLookupService(): UserPrincipalLookupService =
        throw AssertionError()

    override fun close() = throw AssertionError()

    override fun getPathMatcher(syntaxAndPattern: String): PathMatcher = throw AssertionError()

    override fun getRootDirectories(): MutableIterable<Path> = throw AssertionError()
}

private open class StubFileSystemProvider : FileSystemProvider() {
    override fun checkAccess(path: Path, vararg modes: AccessMode) = throw AssertionError()

    override fun copy(source: Path, target: Path, vararg options: CopyOption) =
        throw AssertionError()

    override fun <V : FileAttributeView> getFileAttributeView(
        path: Path,
        type: Class<V>,
        vararg options: LinkOption
    ): V = throw AssertionError()

    override fun isSameFile(path: Path, path2: Path): Boolean = throw AssertionError()

    override fun newFileSystem(uri: URI, env: MutableMap<String, *>): FileSystem =
        throw AssertionError()

    override fun getScheme(): String = throw AssertionError()

    override fun isHidden(path: Path): Boolean = throw AssertionError()

    override fun newDirectoryStream(
        dir: Path,
        filter: DirectoryStream.Filter<in Path>
    ): DirectoryStream<Path> = throw AssertionError()

    override fun newByteChannel(
        path: Path,
        options: MutableSet<out OpenOption>,
        vararg attrs: FileAttribute<*>
    ): SeekableByteChannel = throw AssertionError()

    override fun delete(path: Path) = throw AssertionError()

    override fun <A : BasicFileAttributes> readAttributes(
        path: Path,
        type: Class<A>,
        vararg options: LinkOption
    ): A = throw AssertionError()

    override fun readAttributes(
        path: Path,
        attributes: String,
        vararg options: LinkOption
    ): MutableMap<String, Any> = throw AssertionError()

    override fun getFileSystem(uri: URI): FileSystem = throw AssertionError()

    override fun getPath(uri: URI): Path = throw AssertionError()

    override fun getFileStore(path: Path): FileStore = throw AssertionError()

    override fun setAttribute(
        path: Path,
        attribute: String,
        value: Any,
        vararg options: LinkOption
    ) = throw AssertionError()

    override fun move(source: Path, target: Path, vararg options: CopyOption) =
        throw AssertionError()

    override fun createDirectory(dir: Path, vararg attrs: FileAttribute<*>) =
        throw AssertionError()
}

private open class StubDirectoryStream<T> : DirectoryStream<T> {
    override fun iterator(): MutableIterator<T> = throw AssertionError()

    override fun close() = throw AssertionError()
}
