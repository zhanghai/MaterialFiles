/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote

import android.os.Parcel
import android.os.Parcelable

class ParcelableObject(val value: Any) : Parcelable {
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> value(): T = value as T

    private constructor(source: Parcel) : this(
        source.readParcelable<Parcelable>(ParcelableObject::class.java.classLoader) as Any
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(value as Parcelable, flags)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ParcelableObject> {
            override fun createFromParcel(source: Parcel): ParcelableObject =
                ParcelableObject(source)

            override fun newArray(size: Int): Array<ParcelableObject?> = arrayOfNulls(size)
        }
    }
}

fun Any.toParcelable(): ParcelableObject = ParcelableObject(this)
