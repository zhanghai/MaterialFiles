/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.webdav

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
import me.zhanghai.android.files.provider.webdav.client.Authority
import me.zhanghai.android.files.provider.webdav.client.Client
import me.zhanghai.android.files.util.readParcelable
import okhttp3.HttpUrl
import java.io.File
import java.io.IOException

internal class WebDavPath : ByteStringListPath<WebDavPath>, Client.Path {
    private val fileSystem: WebDavFileSystem

    constructor(
        fileSystem: WebDavFileSystem,
        path: ByteString
    ) : super(WebDavFileSystem.SEPARATOR, path) {
        this.fileSystem = fileSystem
    }

    private constructor(
        fileSystem: WebDavFileSystem,
        absolute: Boolean,
        segments: List<ByteString>
    ) : super(WebDavFileSystem.SEPARATOR, absolute, segments) {
        this.fileSystem = fileSystem
    }

    override fun isPathAbsolute(path: ByteString): Boolean =
        path.isNotEmpty() && path[0] == WebDavFileSystem.SEPARATOR

    override fun createPath(path: ByteString): WebDavPath = WebDavPath(fileSystem, path)

    override fun createPath(absolute: Boolean, segments: List<ByteString>): WebDavPath =
        WebDavPath(fileSystem, absolute, segments)

    override val uriScheme: String
        get() = fileSystem.authority.protocol.scheme

    override val uriAuthority: UriAuthority
        get() = fileSystem.authority.toUriAuthority()

    override val defaultDirectory: WebDavPath
        get() = fileSystem.defaultDirectory

    override fun getFileSystem(): FileSystem = fileSystem

    override fun getRoot(): WebDavPath? = if (isAbsolute) fileSystem.rootDirectory else null

    @Throws(IOException::class)
    override fun toRealPath(vararg options: LinkOption): WebDavPath {
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

    override val authority: Authority
        get() = fileSystem.authority

    override val url: HttpUrl
        get() = HttpUrl.Builder()
            .scheme(authority.protocol.httpScheme)
            .host(authority.host)
            .apply {
                val port = authority.port
                if (port != authority.protocol.defaultPort) {
                    port(port)
                }
            }
            .addPathSegments(toString().removePrefix("/"))
            .build()

    private constructor(source: Parcel) : super(source) {
        fileSystem = source.readParcelable()!!
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)

        dest.writeParcelable(fileSystem, flags)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<WebDavPath> {
            override fun createFromParcel(source: Parcel): WebDavPath = WebDavPath(source)

            override fun newArray(size: Int): Array<WebDavPath?> = arrayOfNulls(size)
        }
    }
}

val Path.isWebDavPath: Boolean
    get() = this is WebDavPath
