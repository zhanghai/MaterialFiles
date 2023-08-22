/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat

fun Parcel.readBooleanCompat(): Boolean = ParcelCompat.readBoolean(this)

fun Parcel.writeBooleanCompat(value: Boolean) {
    ParcelCompat.writeBoolean(this, value)
}

fun <E : Parcelable?, L : MutableList<E>> Parcel.readParcelableListCompat(
    list: L,
    classLoader: ClassLoader?
): L {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        @Suppress("UNCHECKED_CAST")
        return readParcelableList(list, classLoader) as L
    } else {
        val size = readInt()
        if (size == -1) {
            list.clear()
            return list
        }
        val listSize = list.size
        for (index in 0..<size) {
            @Suppress("UNCHECKED_CAST")
            val element = readParcelable<E>(classLoader) as E
            if (index < listSize) {
                list[index] = element
            } else {
                list += element
            }
        }
        if (size < listSize) {
            list.subList(size, listSize).clear()
        }
        return list
    }
}

fun <T : Parcelable?> Parcel.writeParcelableListCompat(value: List<T>?, flags: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        writeParcelableList(value, flags)
    } else {
        if (value == null) {
            writeInt(-1)
            return
        }
        writeInt(value.size)
        for (element in value) {
            writeParcelable(element, flags)
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> Parcel.readSerializableCompat(): T? = readSerializable() as T?
