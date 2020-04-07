/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import me.zhanghai.android.files.provider.common.ParcelableContentProviderFileAttributes

internal class ContentFileAttributes : ParcelableContentProviderFileAttributes {
    constructor(mimeType: String?, size: Long, uri: Uri) : super(
        ContentFileAttributesImpl(mimeType, size, uri)
    )

    private constructor(source: Parcel) : super(source)

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ContentFileAttributes> {
            override fun createFromParcel(source: Parcel): ContentFileAttributes =
                ContentFileAttributes(source)

            override fun newArray(size: Int): Array<ContentFileAttributes?> = arrayOfNulls(size)
        }
    }
}
