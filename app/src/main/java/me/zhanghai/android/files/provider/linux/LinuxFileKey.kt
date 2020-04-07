/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux

import android.os.Parcel
import android.os.Parcelable
import me.zhanghai.android.files.util.hash

internal class LinuxFileKey(
    private val deviceId: Long,
    private val inodeNumber: Long
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as LinuxFileKey
        return deviceId == other.deviceId && inodeNumber == other.inodeNumber
    }

    override fun hashCode(): Int = hash(deviceId, inodeNumber)

    private constructor(source: Parcel) : this(source.readLong(), source.readLong())

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(deviceId)
        dest.writeLong(inodeNumber)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<LinuxFileKey> {
            override fun createFromParcel(source: Parcel): LinuxFileKey = LinuxFileKey(source)

            override fun newArray(size: Int): Array<LinuxFileKey?> = arrayOfNulls(size)
        }
    }
}
