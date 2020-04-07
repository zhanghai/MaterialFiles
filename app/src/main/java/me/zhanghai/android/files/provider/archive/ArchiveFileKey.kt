/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.Path
import me.zhanghai.android.files.util.hash

internal class ArchiveFileKey(
    private val archiveFile: Path,
    private val entryName: String
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as ArchiveFileKey
        return archiveFile == other.archiveFile && entryName == other.entryName
    }

    override fun hashCode(): Int = hash(archiveFile, entryName)

    private constructor(source: Parcel) : this(
        source.readParcelable<Parcelable>(Path::class.java.classLoader) as Path,
        source.readString()!!
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(archiveFile as Parcelable, flags)
        dest.writeString(entryName)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ArchiveFileKey> {
            override fun createFromParcel(source: Parcel): ArchiveFileKey = ArchiveFileKey(source)

            override fun newArray(size: Int): Array<ArchiveFileKey?> = arrayOfNulls(size)
        }
    }
}
