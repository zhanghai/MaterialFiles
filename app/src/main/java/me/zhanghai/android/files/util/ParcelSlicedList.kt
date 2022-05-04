/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.os.Binder
import android.os.Parcel
import android.os.Parcelable

// @see android.content.pm.ParceledListSlice
class ParcelSlicedList<T : Parcelable?> : Parcelable {
    val list: List<T>

    constructor(list: List<T>) {
        this.list = list
    }

    private constructor(source: Parcel) {
        val size = source.readInt()
        val list = ArrayList<T>(size)
        readSliceFromParcel(list, source)
        if (list.size < size) {
            val binder = source.readStrongBinder()
            do {
                Parcel.obtain().use { reply ->
                    Parcel.obtain().use { data ->
                        binder.transact(Binder.FIRST_CALL_TRANSACTION, data, reply, 0)
                    }
                    readSliceFromParcel(list, reply)
                }
            } while (list.size < size)
        }
        this.list = list
    }

    private fun readSliceFromParcel(list: MutableList<T>, source: Parcel) {
        val size = source.readInt()
        repeat(size) {
            @Suppress("UNCHECKED_CAST")
            val element = source.readParcelable<T>(ParcelSlicedList::class.java.classLoader) as T
            list += element
        }
    }

    override fun describeContents(): Int =
        list.fold(0) { contentFlags, parcelable ->
            contentFlags or (parcelable?.describeContents() ?: 0)
        }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        val size = list.size
        dest.writeInt(size)
        val iterator = list.iterator()
        writeSliceToParcel(iterator, dest, flags)
        if (iterator.hasNext()) {
            val writeFlags = flags
            dest.writeStrongBinder(object : Binder() {
                override fun onTransact(
                    code: Int,
                    data: Parcel,
                    reply: Parcel?,
                    flags: Int
                ): Boolean =
                    when (code) {
                        FIRST_CALL_TRANSACTION -> {
                            if (reply != null) {
                                writeSliceToParcel(iterator, reply, writeFlags)
                            }
                            true
                        }
                        else -> super.onTransact(code, data, reply, flags)
                    }
            })
        }
    }

    private fun writeSliceToParcel(iterator: Iterator<T>, dest: Parcel, flags: Int) {
        val startPosition = dest.dataPosition()
        dest.writeInt(0)
        var size = 0
        while (iterator.hasNext() && dest.dataSize() < MAX_IPC_SIZE) {
            val element = iterator.next()
            dest.writeParcelable(element, flags)
            ++size
        }
        val endPosition = dest.dataPosition()
        dest.setDataPosition(startPosition)
        dest.writeInt(size)
        dest.setDataPosition(endPosition)
    }

    companion object {
        // @see IBinder.MAX_IPC_SIZE
        const val MAX_IPC_SIZE = 64 * 1024

        @JvmField
        val CREATOR = object : Parcelable.Creator<ParcelSlicedList<Parcelable?>> {
            override fun createFromParcel(source: Parcel): ParcelSlicedList<Parcelable?> =
                ParcelSlicedList(source)

            override fun newArray(size: Int): Array<ParcelSlicedList<Parcelable?>?> =
                arrayOfNulls(size)
        }
    }
}
