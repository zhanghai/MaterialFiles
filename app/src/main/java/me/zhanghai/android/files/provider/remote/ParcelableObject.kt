/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote

import android.os.Parcel
import android.os.Parcelable

class ParcelableObject(val value: Any?) : Parcelable {
    @Suppress("UNCHECKED_CAST")
    fun <T> value(): T = value as T

    private constructor(
        source: Parcel,
        loader: ClassLoader?
    ) : this(source.readParcelable<Parcelable>(loader))

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(value as Parcelable?, flags)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.ClassLoaderCreator<ParcelableObject> {
            override fun createFromParcel(source: Parcel): ParcelableObject =
                createFromParcel(source, null)

            override fun createFromParcel(source: Parcel, loader: ClassLoader?): ParcelableObject =
                ParcelableObject(source, loader)

            override fun newArray(size: Int): Array<ParcelableObject?> = arrayOfNulls(size)
        }
    }
}

fun Any?.toParcelable(): ParcelableObject = ParcelableObject(this)
