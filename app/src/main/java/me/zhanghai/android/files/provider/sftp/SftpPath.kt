/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.sftp

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
import me.zhanghai.android.files.provider.common.ByteStringBuilder
import me.zhanghai.android.files.provider.common.ByteStringListPath
import me.zhanghai.android.files.provider.common.PollingWatchService
import me.zhanghai.android.files.provider.common.toByteString
import me.zhanghai.android.files.provider.sftp.client.Authority
import me.zhanghai.android.files.provider.sftp.client.Client
import me.zhanghai.android.files.util.readParcelable
import java.io.File
import java.io.IOException

internal class SftpPath : ByteStringListPath<SftpPath>, Client.Path {
    private val fileSystem: SftpFileSystem

    constructor(
        fileSystem: SftpFileSystem,
        path: ByteString
    ) : super(SftpFileSystem.SEPARATOR, path) {
        this.fileSystem = fileSystem
    }

    private constructor(
        fileSystem: SftpFileSystem,
        absolute: Boolean,
        segments: List<ByteString>
    ) : super(SftpFileSystem.SEPARATOR, absolute, segments) {
        this.fileSystem = fileSystem
    }

    override fun isPathAbsolute(path: ByteString): Boolean =
        path.isNotEmpty() && path[0] == SftpFileSystem.SEPARATOR

    override fun createPath(path: ByteString): SftpPath = SftpPath(fileSystem, path)

    override fun createPath(absolute: Boolean, segments: List<ByteString>): SftpPath =
        SftpPath(fileSystem, absolute, segments)

    override val uriSchemeSpecificPart: ByteString
        get() =
            ByteStringBuilder(BYTE_STRING_TWO_SLASHES)
                .append(fileSystem.authority.toString().toByteString())
                .append(super.uriSchemeSpecificPart!!)
                .toByteString()

    override val defaultDirectory: SftpPath
        get() = fileSystem.defaultDirectory

    override fun getFileSystem(): FileSystem = fileSystem

    override fun getRoot(): SftpPath? = if (isAbsolute) fileSystem.rootDirectory else null

    @Throws(IOException::class)
    override fun toRealPath(vararg options: LinkOption): SftpPath {
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
        if (watcher !is PollingWatchService) {
            throw ProviderMismatchException(watcher.toString())
        }
        return watcher.register(this, events, *modifiers)
    }

    override val authority: Authority
        get() = fileSystem.authority

    override val remotePath: String
        get() = toString()

    private constructor(source: Parcel) : super(source) {
        fileSystem = source.readParcelable()!!
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)

        dest.writeParcelable(fileSystem, flags)
    }

    companion object {
        private val BYTE_STRING_TWO_SLASHES = "//".toByteString()

        @JvmField
        val CREATOR = object : Parcelable.Creator<SftpPath> {
            override fun createFromParcel(source: Parcel): SftpPath = SftpPath(source)

            override fun newArray(size: Int): Array<SftpPath?> = arrayOfNulls(size)
        }
    }
}

val Path.isSftpPath: Boolean
    get() = this is SftpPath
