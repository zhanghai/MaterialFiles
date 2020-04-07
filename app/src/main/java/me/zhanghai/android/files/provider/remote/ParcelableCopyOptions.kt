/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.CopyOption
import java8.nio.file.LinkOption
import me.zhanghai.android.files.compat.readSerializableCompat
import java.io.Serializable

class ParcelableCopyOptions(val value: Array<out CopyOption>) : Parcelable {
    private constructor(source: Parcel) : this(
        Array(source.readInt()) {
            when (val type = source.readInt()) {
                0 ->
                    source.readParcelable<Parcelable>(CopyOption::class.java.classLoader)!!
                        as CopyOption
                1 -> source.readSerializableCompat()!!
                else -> throw AssertionError(type)
            }
        }
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(value.size)
        for (option in value) {
            when (option) {
                is Parcelable -> {
                    dest.writeInt(0)
                    dest.writeParcelable(option as Parcelable, flags)
                }
                is Serializable -> {
                    dest.writeInt(1)
                    dest.writeSerializable(option as Serializable)
                }
                else -> throw UnsupportedOperationException(option.toString())
            }
        }
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ParcelableCopyOptions> {
            override fun createFromParcel(source: Parcel): ParcelableCopyOptions =
                ParcelableCopyOptions(source)

            override fun newArray(size: Int): Array<ParcelableCopyOptions?> = arrayOfNulls(size)
        }
    }
}

fun Array<out CopyOption>.toParcelable(): ParcelableCopyOptions = ParcelableCopyOptions(this)

fun Array<out LinkOption>.toParcelable() = (this as Serializable).toParcelable()
