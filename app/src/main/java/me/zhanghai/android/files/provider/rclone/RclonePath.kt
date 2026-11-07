/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.rclone

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.FileSystem
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.ProviderMismatchException
import java8.nio.file.WatchEvent
import java8.nio.file.WatchKey
import java8.nio.file.WatchService
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.ByteStringListPath
import me.zhanghai.android.files.provider.common.LocalWatchService
import me.zhanghai.android.files.provider.common.UriAuthority
import me.zhanghai.android.files.util.readParcelable
import java.io.File
import java.io.IOException

internal class RclonePath : ByteStringListPath<RclonePath> {
    private val fileSystem: RcloneFileSystem

    constructor(
        fileSystem: RcloneFileSystem,
        path: ByteString
    ) : super(RcloneFileSystem.SEPARATOR, path) {
        this.fileSystem = fileSystem
    }

    private constructor(
        fileSystem: RcloneFileSystem,
        absolute: Boolean,
        segments: List<ByteString>
    ) : super(RcloneFileSystem.SEPARATOR, absolute, segments) {
        this.fileSystem = fileSystem
    }

    override fun isPathAbsolute(path: ByteString): Boolean =
        path.isNotEmpty() && path[0] == RcloneFileSystem.SEPARATOR

    override fun createPath(path: ByteString): RclonePath = RclonePath(fileSystem, path)

    override fun createPath(absolute: Boolean, segments: List<ByteString>): RclonePath =
        RclonePath(fileSystem, absolute, segments)

    override val uriScheme: String
        get() = RcloneFileSystemProvider.scheme

    override val uriAuthority: UriAuthority
        get() = fileSystem.authority.toUriAuthority()

    override val defaultDirectory: RclonePath
        get() = fileSystem.defaultDirectory

    override fun getFileSystem(): FileSystem = fileSystem

    override fun getRoot(): RclonePath? = if (isAbsolute) fileSystem.rootDirectory else null

    @Throws(IOException::class)
    override fun toRealPath(vararg options: LinkOption): RclonePath {
        throw UnsupportedOperationException()
    }

    override fun toFile(): File {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun register(
        watcher: WatchService,
        events: Array<WatchEvent.Kind<*>>,
        vararg modifiers: WatchEvent.Modifier
    ): WatchKey {
        if (watcher !is LocalWatchService) {
            throw ProviderMismatchException(watcher.toString())
        }
        return watcher.register(this, events, *modifiers)
    }

    val remoteName: String
        get() = fileSystem.authority.remoteName

    val remotePath: String
        get() = toString().trimStart('/')

    private constructor(source: Parcel) : super(source) {
        fileSystem = source.readParcelable()!!
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)

        dest.writeParcelable(fileSystem, flags)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<RclonePath> {
            override fun createFromParcel(source: Parcel): RclonePath = RclonePath(source)

            override fun newArray(size: Int): Array<RclonePath?> = arrayOfNulls(size)
        }
    }
}
