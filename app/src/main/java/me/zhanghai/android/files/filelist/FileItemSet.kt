/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.Path
import me.zhanghai.android.files.compat.writeParcelableListCompat
import me.zhanghai.android.files.file.FileItem
import me.zhanghai.android.files.util.LinkedMapSet
import me.zhanghai.android.files.util.readParcelableListCompat

class FileItemSet() : LinkedMapSet<Path, FileItem>(FileItem::path), Parcelable {
    private constructor(source: Parcel, loader: ClassLoader?) : this() {
        addAll(source.readParcelableListCompat(loader))
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelableListCompat(toList(), flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.ClassLoaderCreator<FileItemSet> {
        override fun createFromParcel(source: Parcel): FileItemSet = createFromParcel(source, null)

        override fun createFromParcel(source: Parcel, loader: ClassLoader?): FileItemSet =
            FileItemSet(source, loader)

        override fun newArray(size: Int): Array<FileItemSet?> = arrayOfNulls(size)
    }
}

fun fileItemSetOf(vararg files: FileItem) = FileItemSet().apply { addAll(files) }
