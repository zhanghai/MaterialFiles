/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parceler
import me.zhanghai.android.files.compat.writeParcelableListCompat

object ParcelableParceler : Parceler<Any?> {
    override fun create(parcel: Parcel): Any? =
        parcel.readParcelable(ParcelableParceler::class.java.classLoader)

    override fun Any?.write(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(this as Parcelable?, flags)
    }
}

object ParcelableListParceler : Parceler<List<Any?>> {
    override fun create(parcel: Parcel): List<Any?> =
        parcel.readParcelableListCompat(ParcelableParceler::class.java.classLoader)

    override fun List<Any?>.write(parcel: Parcel, flags: Int) {
        @Suppress("UNCHECKED_CAST")
        parcel.writeParcelableListCompat(this as List<Parcelable?>, flags)
    }
}

object ValueParceler : Parceler<Any?> {
    override fun create(parcel: Parcel): Any? =
        parcel.readValue(ParcelableParceler::class.java.classLoader)

    override fun Any?.write(parcel: Parcel, flags: Int) {
        parcel.writeValue(this)
    }
}
