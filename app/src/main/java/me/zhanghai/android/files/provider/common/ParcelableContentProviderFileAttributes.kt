/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.attribute.FileTime
import me.zhanghai.android.files.util.readParcelable

abstract class ParcelableContentProviderFileAttributes : ContentProviderFileAttributes, Parcelable {
    private val lastModifiedTime: FileTime
    private val lastAccessTime: FileTime
    private val creationTime: FileTime
    private val mimeType: String?
    private val size: Long
    private val fileKey: Parcelable

    constructor(attributes: ContentProviderFileAttributes) {
        lastModifiedTime = attributes.lastModifiedTime()
        lastAccessTime = attributes.lastAccessTime()
        creationTime = attributes.creationTime()
        mimeType = attributes.mimeType()
        size = attributes.size()
        fileKey = attributes.fileKey() as Parcelable
    }

    override fun lastModifiedTime(): FileTime = lastModifiedTime

    override fun lastAccessTime(): FileTime = lastAccessTime

    override fun creationTime(): FileTime = creationTime

    override fun mimeType(): String? = mimeType

    override fun size(): Long = size

    override fun fileKey(): Parcelable = fileKey

    protected constructor(source: Parcel) {
        lastModifiedTime = source.readParcelable<ParcelableFileTime>()!!.value
        lastAccessTime = source.readParcelable<ParcelableFileTime>()!!.value
        creationTime = source.readParcelable<ParcelableFileTime>()!!.value
        mimeType = source.readString()
        size = source.readLong()
        fileKey = source.readParcelable(javaClass.classLoader)!!
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(lastModifiedTime.toParcelable(), flags)
        dest.writeParcelable(lastAccessTime.toParcelable(), flags)
        dest.writeParcelable(creationTime.toParcelable(), flags)
        dest.writeString(mimeType)
        dest.writeLong(size)
        dest.writeParcelable(fileKey, flags)
    }
}
