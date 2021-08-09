/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.smb

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
import me.zhanghai.android.files.provider.common.toByteString
import me.zhanghai.android.files.provider.smb.client.Authority
import me.zhanghai.android.files.provider.smb.client.Client
import me.zhanghai.android.files.util.readParcelable
import java.io.File
import java.io.IOException

internal class SmbPath : ByteStringListPath<SmbPath>, Client.Path {
    private val fileSystem: SmbFileSystem

    constructor(
        fileSystem: SmbFileSystem,
        path: ByteString
    ) : super(SmbFileSystem.SEPARATOR, path) {
        this.fileSystem = fileSystem
    }

    private constructor(
        fileSystem: SmbFileSystem,
        absolute: Boolean,
        segments: List<ByteString>
    ) : super(SmbFileSystem.SEPARATOR, absolute, segments) {
        this.fileSystem = fileSystem
    }

    override fun isPathAbsolute(path: ByteString): Boolean =
        path.isNotEmpty() && path[0] == SmbFileSystem.SEPARATOR

    override fun createPath(path: ByteString): SmbPath = SmbPath(fileSystem, path)

    override fun createPath(absolute: Boolean, segments: List<ByteString>): SmbPath =
        SmbPath(fileSystem, absolute, segments)

    override val uriSchemeSpecificPart: ByteString
        get() =
            ByteStringBuilder(BYTE_STRING_TWO_SLASHES)
                .append(fileSystem.authority.toString().toByteString())
                .append(super.uriSchemeSpecificPart!!)
                .toByteString()

    override val defaultDirectory: SmbPath
        get() = fileSystem.defaultDirectory

    override fun getFileSystem(): FileSystem = fileSystem

    override fun getRoot(): SmbPath? = if (isAbsolute) fileSystem.rootDirectory else null

    @Throws(IOException::class)
    override fun toRealPath(vararg options: LinkOption): SmbPath {
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
        if (watcher !is SmbWatchService) {
            throw ProviderMismatchException(watcher.toString())
        }
        return watcher.register(this, events, *modifiers)
    }

    override val authority: Authority
        get() = fileSystem.authority

    override val sharePath: Client.Path.SharePath? by lazy {
        check(isAbsolute)
        if (nameCount > 0) {
            Client.Path.SharePath(
                getNameByteString(0).toString(),
                nameByteStrings.asSequence().drop(1).joinToString("\\")
            )
        } else {
            null
        }
    }

    fun toWindowsPath(): String =
        if (isAbsolute) {
            // Port cannot be specified in a Windows UNC path for SMB, or otherwise it is resolved
            // as a WebDAV path.
            check(authority.port == Authority.DEFAULT_PORT) {
                "Path is absolute but uses port ${authority.port} instead of the default port ${
                Authority.DEFAULT_PORT}"
            }
            StringBuilder()
                .append("\\\\")
                .append(authority.host)
                .append("\\")
                .apply {
                    val share = sharePath
                    if (share != null) {
                        append(share.name)
                        append("\\")
                        append(share.path)
                    }
                }
                .toString()
        } else {
            nameByteStrings.joinToString("\\")
        }

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
        val CREATOR = object : Parcelable.Creator<SmbPath> {
            override fun createFromParcel(source: Parcel): SmbPath = SmbPath(source)

            override fun newArray(size: Int): Array<SmbPath?> = arrayOfNulls(size)
        }
    }
}

val Path.isSmbPath: Boolean
    get() = this is SmbPath
