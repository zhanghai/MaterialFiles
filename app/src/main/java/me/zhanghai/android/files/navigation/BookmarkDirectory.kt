/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation

import android.os.Parcelable
import java8.nio.file.Path
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.WriteWith
import me.zhanghai.android.files.filelist.name
import me.zhanghai.android.files.util.ParcelableParceler
import java.util.Random

@Parcelize
@Suppress("DataClassPrivateConstructor")
data class BookmarkDirectory private constructor(
    val id: Long,
    private val customName: String?,
    val path: @WriteWith<ParcelableParceler> Path
) : Parcelable {
    // We cannot simply use path.hashCode() as ID because different bookmark directories may have
    // the same path.
    constructor(customName: String?, path: Path) : this(Random().nextLong(), customName, path)

    val name: String
        get() = if (!customName.isNullOrEmpty()) customName else path.name
}
