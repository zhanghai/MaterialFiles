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

    private constructor(source: Parcel, loader: ClassLoader?) : super(source, loader)

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.ClassLoaderCreator<PosixGroup> {
            override fun createFromParcel(source: Parcel): PosixGroup =
                createFromParcel(source, null)

            override fun createFromParcel(source: Parcel, loader: ClassLoader?): PosixGroup =
                PosixGroup(source, loader)

            override fun newArray(size: Int): Array<PosixGroup?> = arrayOfNulls(size)
        }
    }
}
