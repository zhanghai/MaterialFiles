/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.Path
import me.zhanghai.android.files.provider.common.ParcelablePosixFileAttributes
import org.apache.commons.compress.archivers.ArchiveEntry

internal class ArchiveFileAttributes : ParcelablePosixFileAttributes {
    val entryName: String

    constructor(archiveFile: Path, entry: ArchiveEntry) : this(
        ArchiveFileAttributesImpl(archiveFile, entry)
    )

    private constructor(attributes: ArchiveFileAttributesImpl) : super(attributes) {
        entryName = attributes.entryName
    }

    private constructor(source: Parcel) : super(source) {
        entryName = source.readString()!!
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)

        dest.writeString(entryName)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ArchiveFileAttributes> {
            override fun createFromParcel(source: Parcel): ArchiveFileAttributes =
                ArchiveFileAttributes(source)

            override fun newArray(size: Int): Array<ArchiveFileAttributes?> = arrayOfNulls(size)
        }
    }
}
