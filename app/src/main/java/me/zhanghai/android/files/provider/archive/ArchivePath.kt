/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.WatchEvent
import java8.nio.file.WatchKey
import java8.nio.file.WatchService
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.ByteStringListPath
import me.zhanghai.android.files.provider.common.toByteString
import me.zhanghai.android.files.provider.root.RootStrategy
import me.zhanghai.android.files.provider.root.RootablePath
import me.zhanghai.android.files.util.readParcelable
import java.io.File
import java.io.IOException

internal class ArchivePath : ByteStringListPath<ArchivePath>, RootablePath {
    private val fileSystem: ArchiveFileSystem

    constructor(fileSystem: ArchiveFileSystem, path: ByteString) : super(
        ArchiveFileSystem.SEPARATOR, path
    ) {
        this.fileSystem = fileSystem
    }

    private constructor(
        fileSystem: ArchiveFileSystem,
        absolute: Boolean,
        segments: List<ByteString>
    ) : super(ArchiveFileSystem.SEPARATOR, absolute, segments) {
        this.fileSystem = fileSystem
    }

    override fun isPathAbsolute(path: ByteString): Boolean =
        !path.isEmpty() && path[0] == ArchiveFileSystem.SEPARATOR

    override fun createPath(path: ByteString): ArchivePath = ArchivePath(fileSystem, path)

    override fun createPath(absolute: Boolean, segments: List<ByteString>): ArchivePath =
        ArchivePath(fileSystem, absolute, segments)

    override val uriSchemeSpecificPart: ByteString?
        get() = fileSystem.archiveFile.toUri().toString().toByteString()

    override val uriFragment: ByteString?
        get() = super.uriSchemeSpecificPart

    override val defaultDirectory: ArchivePath
        get() = fileSystem.defaultDirectory

    override fun getFileSystem(): ArchiveFileSystem = fileSystem

    override fun getRoot(): ArchivePath? = if (isAbsolute) fileSystem.rootDirectory else null

    @Throws(IOException::class)
    override fun toRealPath(vararg options: LinkOption): ArchivePath {
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
        throw UnsupportedOperationException()
    }

    override var isRootPreferred: Boolean
        get() {
            val archiveFile = fileSystem.archiveFile
            return if (archiveFile is RootablePath) archiveFile.isRootPreferred else false
        }
        set(value) {
            val archiveFile = fileSystem.archiveFile
            if (archiveFile is RootablePath) {
                archiveFile.isRootPreferred = value
            }
        }

    override val rootStrategy: RootStrategy
        get() {
            val archiveFile = fileSystem.archiveFile
            if (archiveFile !is RootablePath) {
                return RootStrategy.NEVER
            }
            val rootablePath = archiveFile as RootablePath
            return rootablePath.rootStrategy
        }

    private constructor(source: Parcel) : super(source) {
        fileSystem = source.readParcelable()!!
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)

        dest.writeParcelable(fileSystem, flags)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ArchivePath> {
            override fun createFromParcel(source: Parcel): ArchivePath = ArchivePath(source)

            override fun newArray(size: Int): Array<ArchivePath?> = arrayOfNulls(size)
        }
    }
}

val Path.isArchivePath: Boolean
    get() = this is ArchivePath
