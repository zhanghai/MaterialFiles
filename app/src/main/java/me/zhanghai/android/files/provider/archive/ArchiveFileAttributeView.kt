/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive

import android.os.Parcel
import android.os.Parcelable
import me.zhanghai.android.files.provider.root.RootablePosixFileAttributeView

internal class ArchiveFileAttributeView(
    private val path: ArchivePath
) : RootablePosixFileAttributeView(
    path, LocalArchiveFileAttributeView(path), { RootArchiveFileAttributeView(it, path) }
) {
    private constructor(
        source: Parcel,
        loader: ClassLoader?
    ) : this(source.readParcelable(loader)!!)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(path as Parcelable, flags)
    }

    companion object {
        val SUPPORTED_NAMES = LocalArchiveFileAttributeView.SUPPORTED_NAMES

        @JvmField
        val CREATOR = object : Parcelable.ClassLoaderCreator<ArchiveFileAttributeView> {
            override fun createFromParcel(source: Parcel): ArchiveFileAttributeView =
                createFromParcel(source, null)

            override fun createFromParcel(
                source: Parcel,
                loader: ClassLoader?
            ): ArchiveFileAttributeView = ArchiveFileAttributeView(source, loader)

            override fun newArray(size: Int): Array<ArchiveFileAttributeView?> = arrayOfNulls(size)
        }
    }
}
