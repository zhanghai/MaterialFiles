/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.mediastore

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.FileSystem
import java8.nio.file.InvalidPathException
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.WatchEvent
import java8.nio.file.WatchKey
import java8.nio.file.WatchService
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.ByteStringListPath
import me.zhanghai.android.files.provider.common.toByteString
import me.zhanghai.android.files.util.readParcelable
import java.io.File

class MediaStorePath : ByteStringListPath<MediaStorePath> {
    private val fileSystem: MediaStoreFileSystem
    val category: MediaStoreCategory

    constructor(
        fileSystem: MediaStoreFileSystem,
        category: MediaStoreCategory
    ) : super(
        '/'.code.toByte(),
        true,
        listOf(category.segment.toByteString())
    ) {
        this.fileSystem = fileSystem
        this.category = category
    }

    override fun isPathAbsolute(path: ByteString): Boolean =
        path.startsWith("/".toByteString())

    override fun createPath(path: ByteString): MediaStorePath {
        val str = path.toString().trimStart('/')
        val cat = MediaStoreCategory.fromSegment(str)
            ?: throw InvalidPathException(path.toString(), "Unknown MediaStore category: $str")
        return MediaStorePath(fileSystem, cat)
    }

    override fun createPath(absolute: Boolean, segments: List<ByteString>): MediaStorePath {
        if (absolute && segments.size == 1) {
            val cat = MediaStoreCategory.fromSegment(segments[0].toString())
            if (cat != null) return MediaStorePath(fileSystem, cat)
        }
        throw UnsupportedOperationException(
            "Cannot create MediaStorePath: absolute=$absolute, segments=$segments"
        )
    }

    override val defaultDirectory: MediaStorePath
        get() = throw UnsupportedOperationException()

    override fun getFileSystem(): FileSystem = fileSystem

    override fun getRoot(): MediaStorePath = this

    override fun getParent(): MediaStorePath? = null

    override fun normalize(): MediaStorePath = this

    override fun toAbsolutePath(): MediaStorePath {
        check(isAbsolute) { "MediaStorePath must always be absolute" }
        return this
    }

    override fun toRealPath(vararg options: LinkOption): MediaStorePath {
        throw UnsupportedOperationException()
    }

    override fun toFile(): File {
        throw UnsupportedOperationException()
    }

    override fun register(
        watcher: WatchService,
        events: Array<WatchEvent.Kind<*>>,
        vararg modifiers: WatchEvent.Modifier
    ): WatchKey {
        throw UnsupportedOperationException()
    }

    private constructor(source: Parcel) : super(source) {
        fileSystem = source.readParcelable()!!
        val categoryName = source.readString()!!
        category = MediaStoreCategory.valueOf(categoryName)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeParcelable(fileSystem, flags)
        dest.writeString(category.name)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<MediaStorePath> {
            override fun createFromParcel(source: Parcel): MediaStorePath = MediaStorePath(source)
            override fun newArray(size: Int): Array<MediaStorePath?> = arrayOfNulls(size)
        }
    }
}

val Path.isMediaStorePath: Boolean
    get() = this is MediaStorePath
