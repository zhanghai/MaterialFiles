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
import me.zhanghai.android.files.compat.writeParcelableListCompat
import me.zhanghai.android.files.provider.common.PathListDirectoryStream
import me.zhanghai.android.files.util.readParcelableListCompat
import java.io.IOException

class ParcelableDirectoryStream : Parcelable {
    private val paths: List<Path>

    val value: DirectoryStream<Path>
        get() = PathListDirectoryStream(paths, DirectoryStream.Filter { true })

    @Throws(IOException::class)
    constructor(value: DirectoryStream<Path>) {
        paths = try {
            value.toList()
        } catch (e: DirectoryIteratorException) {
            throw e.cause!!
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        @Suppress("UNCHECKED_CAST")
        dest.writeParcelableListCompat(paths as List<Parcelable>, flags)
    }

    private constructor(source: Parcel) {
        @Suppress("UNCHECKED_CAST")
        paths = source.readParcelableListCompat<Parcelable>(Path::class.java.classLoader)
            as List<Path>
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ParcelableDirectoryStream> {
            override fun createFromParcel(source: Parcel): ParcelableDirectoryStream =
                ParcelableDirectoryStream(source)

            override fun newArray(size: Int): Array<ParcelableDirectoryStream?> = arrayOfNulls(size)
        }
    }
}
