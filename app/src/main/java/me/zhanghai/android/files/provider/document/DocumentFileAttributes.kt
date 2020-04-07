/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.document

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import me.zhanghai.android.files.provider.common.ParcelableContentProviderFileAttributes

internal class DocumentFileAttributes : ParcelableContentProviderFileAttributes {
    val flags: Int

    constructor(
        lastModifiedTimeMillis: Long,
        mimeType: String?,
        size: Long,
        flags: Int,
        uri: Uri
    ) : this(DocumentFileAttributesImpl(lastModifiedTimeMillis, mimeType, size, flags, uri))

    private constructor(attributes: DocumentFileAttributesImpl) : super(attributes) {
        flags = attributes.flags
    }

    private constructor(source: Parcel) : super(source) {
        flags = source.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)

        dest.writeInt(this.flags)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<DocumentFileAttributes> {
            override fun createFromParcel(source: Parcel): DocumentFileAttributes =
                DocumentFileAttributes(source)

            override fun newArray(size: Int): Array<DocumentFileAttributes?> = arrayOfNulls(size)
        }
    }
}
