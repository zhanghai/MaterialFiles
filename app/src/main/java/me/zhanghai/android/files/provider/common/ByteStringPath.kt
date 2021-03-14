/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.FileSystem
import java8.nio.file.LinkOption
import java8.nio.file.Path
import java8.nio.file.WatchEvent
import java8.nio.file.WatchKey
import java8.nio.file.WatchService
import java.io.File
import java.net.URI

class ByteStringPath(private val byteString: ByteString) : Parcelable, Path {
    fun toByteString(): ByteString = byteString

    override fun toString(): String = byteString.toString()

    override fun getFileSystem(): FileSystem {
        throw UnsupportedOperationException()
    }

    override fun isAbsolute(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getRoot(): Path {
        throw UnsupportedOperationException()
    }

    override fun getFileName(): Path {
        throw UnsupportedOperationException()
    }

    override fun getParent(): Path {
        throw UnsupportedOperationException()
    }

    override fun getNameCount(): Int {
        throw UnsupportedOperationException()
    }

    override fun getName(index: Int): Path {
        throw UnsupportedOperationException()
    }

    override fun subpath(beginIndex: Int, endIndex: Int): Path {
        throw UnsupportedOperationException()
    }

    override fun startsWith(other: Path): Boolean {
        throw UnsupportedOperationException()
    }

    override fun startsWith(other: String): Boolean {
        throw UnsupportedOperationException()
    }

    override fun endsWith(other: Path): Boolean {
        throw UnsupportedOperationException()
    }

    override fun endsWith(other: String): Boolean {
        throw UnsupportedOperationException()
    }

    override fun normalize(): Path {
        throw UnsupportedOperationException()
    }

    override fun resolve(other: Path): Path {
        throw UnsupportedOperationException()
    }

    override fun resolve(other: String): Path {
        throw UnsupportedOperationException()
    }

    override fun resolveSibling(other: Path): Path {
        throw UnsupportedOperationException()
    }

    override fun resolveSibling(other: String): Path {
        throw UnsupportedOperationException()
    }

    override fun relativize(other: Path): Path {
        throw UnsupportedOperationException()
    }

    override fun toUri(): URI {
        throw UnsupportedOperationException()
    }

    override fun toAbsolutePath(): Path {
        throw UnsupportedOperationException()
    }

    override fun toRealPath(vararg options: LinkOption): Path {
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

    override fun register(watcher: WatchService, vararg events: WatchEvent.Kind<*>): WatchKey {
        throw UnsupportedOperationException()
    }

    override fun iterator(): MutableIterator<Path> {
        throw UnsupportedOperationException()
    }

    override fun compareTo(other: Path): Int {
        throw UnsupportedOperationException()
    }

    private constructor(
        source: Parcel,
        loader: ClassLoader?
    ) : this(source.readParcelable(loader)!!)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(byteString, flags)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.ClassLoaderCreator<ByteStringPath> {
            override fun createFromParcel(source: Parcel): ByteStringPath =
                createFromParcel(source, null)

            override fun createFromParcel(source: Parcel, loader: ClassLoader?): ByteStringPath =
                ByteStringPath(source, loader)

            override fun newArray(size: Int): Array<ByteStringPath?> = arrayOfNulls(size)
        }
    }
}
