/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.attribute.UserPrincipal

class PosixUser : PosixPrincipal, UserPrincipal {
    constructor(id: Int, name: ByteString?) : super(id, name)

    private constructor(source: Parcel, loader: ClassLoader?) : super(source, loader)

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.ClassLoaderCreator<PosixUser> {
            override fun createFromParcel(source: Parcel): PosixUser =
                createFromParcel(source, null)

            override fun createFromParcel(source: Parcel, loader: ClassLoader?): PosixUser =
                PosixUser(source, loader)

            override fun newArray(size: Int): Array<PosixUser?> = arrayOfNulls(size)
        }
    }
}
