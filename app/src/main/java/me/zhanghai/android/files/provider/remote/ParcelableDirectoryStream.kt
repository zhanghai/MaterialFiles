/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.DirectoryIteratorException
import java8.nio.file.DirectoryStream
import java8.nio.file.Path
import me.zhanghai.android.files.provider.common.PathListDirectoryStream
import me.zhanghai.android.files.util.ParcelSlicedList
import java.io.IOException

class ParcelableDirectoryStream : Parcelable {
    private val paths: List<Path>

    val value: DirectoryStream<Path>
        get() = PathListDirectoryStream(paths) { true }

    @Throws(IOException::class)
    constructor(value: DirectoryStream<Path>) {
        paths = try {
            value.toList()
        } catch (e: DirectoryIteratorException) {
            throw e.cause!!
        }
    }

    private constructor(source: Parcel, loader: ClassLoader?) {
        @Suppress("UNCHECKED_CAST")
        paths = source.readParcelable<ParcelSlicedList<Parcelable>>(loader)!!.list as List<Path>
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        @Suppress("UNCHECKED_CAST")
        dest.writeParcelable(ParcelSlicedList(paths as List<Parcelable>), flags)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.ClassLoaderCreator<ParcelableDirectoryStream> {
            override fun createFromParcel(source: Parcel): ParcelableDirectoryStream =
                createFromParcel(source, null)

            override fun createFromParcel(
                source: Parcel,
                loader: ClassLoader?
            ): ParcelableDirectoryStream = ParcelableDirectoryStream(source, loader)

            override fun newArray(size: Int): Array<ParcelableDirectoryStream?> = arrayOfNulls(size)
        }
    }
}
