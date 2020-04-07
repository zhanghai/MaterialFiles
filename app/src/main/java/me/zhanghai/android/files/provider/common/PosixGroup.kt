/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.attribute.GroupPrincipal

class PosixGroup : PosixPrincipal, GroupPrincipal {
    constructor(id: Int, name: ByteString?) : super(id, name)

    private constructor(source: Parcel) : super(source)

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<PosixGroup> {
            override fun createFromParcel(source: Parcel): PosixGroup = PosixGroup(source)

            override fun newArray(size: Int): Array<PosixGroup?> = arrayOfNulls(size)
        }
    }
}
