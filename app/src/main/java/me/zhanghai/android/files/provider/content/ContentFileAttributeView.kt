/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.attribute.BasicFileAttributeView
import java8.nio.file.attribute.FileTime
import me.zhanghai.android.files.provider.content.resolver.Resolver
import me.zhanghai.android.files.provider.content.resolver.ResolverException
import java.io.IOException

internal class ContentFileAttributeView(
    private val path: ContentPath
) : BasicFileAttributeView, Parcelable {
    override fun name(): String = NAME

    @Throws(IOException::class)
    override fun readAttributes(): ContentFileAttributes {
        val uri = path.uri!!
        val mimeType = try {
            Resolver.getMimeType(uri)
        } catch (e: ResolverException) {
            throw e.toFileSystemException(path.toString())
        }
        val size = try {
            Resolver.getSize(uri)
        } catch (e: ResolverException) {
            throw e.toFileSystemException(path.toString())
        }
        return ContentFileAttributes.from(mimeType, size, uri)
    }

    override fun setTimes(
        lastModifiedTime: FileTime?,
        lastAccessTime: FileTime?,
        createTime: FileTime?
    ) {
        throw UnsupportedOperationException()
    }

    private constructor(
        source: Parcel,
        loader: ClassLoader?
    ) : this(source.readParcelable(loader)!!)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(path as Parcelable, flags)
    }

    companion object {
        private val NAME = ContentFileSystemProvider.scheme

        val SUPPORTED_NAMES = setOf("basic", NAME)

        @JvmField
        val CREATOR = object : Parcelable.ClassLoaderCreator<ContentFileAttributeView> {
            override fun createFromParcel(source: Parcel): ContentFileAttributeView =
                createFromParcel(source, null)

            override fun createFromParcel(
                source: Parcel,
                loader: ClassLoader?
            ): ContentFileAttributeView = ContentFileAttributeView(source, loader)

            override fun newArray(size: Int): Array<ContentFileAttributeView?> = arrayOfNulls(size)
        }
    }
}
