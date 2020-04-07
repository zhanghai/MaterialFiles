/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.attribute.FileTime
import me.zhanghai.android.files.compat.readSerializableCompat
import me.zhanghai.android.files.util.readParcelable

abstract class ParcelablePosixFileAttributes : Parcelable, PosixFileAttributes {
    private val lastModifiedTime: FileTime
    private val lastAccessTime: FileTime
    private val creationTime: FileTime
    private val type: PosixFileType
    private val size: Long
    private val fileKey: Parcelable
    private val owner: PosixUser?
    private val group: PosixGroup?
    private val mode: Set<PosixFileModeBit>?
    private val seLinuxContext: ByteString?

    constructor(attributes: PosixFileAttributes) {
        lastModifiedTime = attributes.lastModifiedTime()
        lastAccessTime = attributes.lastAccessTime()
        creationTime = attributes.creationTime()
        type = attributes.type()
        size = attributes.size()
        fileKey = attributes.fileKey()
        owner = attributes.owner()
        group = attributes.group()
        mode = attributes.mode()
        seLinuxContext = attributes.seLinuxContext()
    }

    override fun lastModifiedTime(): FileTime = lastModifiedTime

    override fun lastAccessTime(): FileTime = lastAccessTime

    override fun creationTime(): FileTime = creationTime

    override fun type(): PosixFileType = type

    override fun size(): Long = size

    override fun fileKey(): Parcelable = fileKey

    override fun owner(): PosixUser? = owner

    override fun group(): PosixGroup? = group

    override fun mode(): Set<PosixFileModeBit>? = mode

    override fun seLinuxContext(): ByteString? = seLinuxContext

    protected constructor(source: Parcel) {
        lastModifiedTime = source.readParcelable<ParcelableFileTime>()!!.value
        lastAccessTime = source.readParcelable<ParcelableFileTime>()!!.value
        creationTime = source.readParcelable<ParcelableFileTime>()!!.value
        type = source.readSerializableCompat()!!
        size = source.readLong()
        fileKey = source.readParcelable(javaClass.classLoader)!!
        owner = source.readParcelable()
        group = source.readParcelable()
        mode = source.readParcelable<ParcelablePosixFileMode>()?.value
        seLinuxContext = source.readParcelable()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(lastModifiedTime.toParcelable(), flags)
        dest.writeParcelable(lastAccessTime.toParcelable(), flags)
        dest.writeParcelable(creationTime.toParcelable(), flags)
        dest.writeSerializable(type)
        dest.writeLong(size)
        dest.writeParcelable(fileKey, flags)
        dest.writeParcelable(owner, flags)
        dest.writeParcelable(group, flags)
        dest.writeParcelable(mode?.toParcelable(), flags)
        dest.writeParcelable(seLinuxContext, flags)
    }
}
