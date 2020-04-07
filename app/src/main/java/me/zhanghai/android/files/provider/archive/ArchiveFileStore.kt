/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.Path
import me.zhanghai.android.files.provider.root.RootPosixFileStore
import me.zhanghai.android.files.provider.root.RootablePosixFileStore

internal class ArchiveFileStore(private val archiveFile: Path) : RootablePosixFileStore(
    archiveFile, LocalArchiveFileStore(archiveFile), { RootPosixFileStore(it) }
) {
    private constructor(source: Parcel) : this(
        source.readParcelable<Parcelable>(Path::class.java.classLoader) as Path
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(archiveFile as Parcelable, flags)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ArchiveFileStore> {
            override fun createFromParcel(source: Parcel): ArchiveFileStore =
                ArchiveFileStore(source)

            override fun newArray(size: Int): Array<ArchiveFileStore?> = arrayOfNulls(size)
        }
    }
}
