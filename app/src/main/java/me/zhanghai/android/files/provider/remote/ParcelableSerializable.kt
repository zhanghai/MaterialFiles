/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class ParcelableSerializable(private val value: Serializable) : Parcelable {
    @Suppress("UNCHECKED_CAST")
    fun <T> value(): T = value as T

    private constructor(source: Parcel) : this(source.readSerializable()!!)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeSerializable(value)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ParcelableSerializable> {
            override fun createFromParcel(source: Parcel): ParcelableSerializable =
                ParcelableSerializable(source)

            override fun newArray(size: Int): Array<ParcelableSerializable?> = arrayOfNulls(size)
        }
    }
}

fun Serializable.toParcelable(): ParcelableSerializable = ParcelableSerializable(this)
