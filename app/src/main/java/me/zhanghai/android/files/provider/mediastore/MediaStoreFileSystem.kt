/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.mediastore

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.FileStore
import java8.nio.file.FileSystem
import java8.nio.file.Path
import java8.nio.file.PathMatcher
import java8.nio.file.WatchService
import java8.nio.file.attribute.UserPrincipalLookupService
import java8.nio.file.spi.FileSystemProvider
import me.zhanghai.android.files.provider.common.ByteString
import me.zhanghai.android.files.provider.common.ByteStringListPathCreator
import java.io.IOException

class MediaStoreFileSystem(
    private val provider: MediaStoreFileSystemProvider
) : FileSystem(), ByteStringListPathCreator, Parcelable {

    override fun provider(): FileSystemProvider = provider

    override fun close() {
        throw UnsupportedOperationException()
    }

    override fun isOpen(): Boolean = true

    override fun isReadOnly(): Boolean = true

    override fun getSeparator(): String = "/"

    override fun getRootDirectories(): Iterable<Path> =
        MediaStoreCategory.values().map { MediaStorePath(this, it) }

    override fun getFileStores(): Iterable<FileStore> {
        throw UnsupportedOperationException()
    }

    override fun supportedFileAttributeViews(): Set<String> = emptySet()

    override fun getPath(first: String, vararg more: String): MediaStorePath {
        if (more.isNotEmpty()) throw UnsupportedOperationException()
        val segment = first.trimStart('/')
        val category = MediaStoreCategory.fromSegment(segment)
            ?: throw IllegalArgumentException("Unknown MediaStore category: $first")
        return MediaStorePath(this, category)
    }

    override fun getPath(first: ByteString, vararg more: ByteString): MediaStorePath =
        getPath(first.toString())

    override fun getPathMatcher(syntaxAndPattern: String): PathMatcher {
        throw UnsupportedOperationException()
    }

    override fun getUserPrincipalLookupService(): UserPrincipalLookupService {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    override fun newWatchService(): WatchService {
        throw UnsupportedOperationException()
    }

    fun getCategoryPath(category: MediaStoreCategory): MediaStorePath =
        MediaStorePath(this, category)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {}

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<MediaStoreFileSystem> {
            override fun createFromParcel(source: Parcel): MediaStoreFileSystem =
                MediaStoreFileSystemProvider.fileSystem

            override fun newArray(size: Int): Array<MediaStoreFileSystem?> = arrayOfNulls(size)
        }
    }
}
