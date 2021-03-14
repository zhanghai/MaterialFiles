/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.CopyOption
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.RemoteCallback
import me.zhanghai.android.files.util.getArgs
import me.zhanghai.android.files.util.putArgs

class ProgressCopyOption(
    val intervalMillis: Long,
    val listener: (Long) -> Unit
) : CopyOption, Parcelable {
    private constructor(source: Parcel, loader: ClassLoader?) : this(
        source.readLong(),
        source.readParcelable<RemoteCallback>(loader)!!.let {
            // TODO: kotlinc: Cannot infer a type for this parameter. Please specify it explicitly.
            //{ copiedSize -> it.sendResult(Bundle().putArgs(ListenerArgs(copiedSize))) }
            { copiedSize: Long -> it.sendResult(Bundle().putArgs(ListenerArgs(copiedSize))) }
        }
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(intervalMillis)
        dest.writeParcelable(
            RemoteCallback { listener(it.getArgs<ListenerArgs>().copiedSize) }, flags
        )
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.ClassLoaderCreator<ProgressCopyOption> {
            override fun createFromParcel(source: Parcel): ProgressCopyOption =
                createFromParcel(source, null)

            override fun createFromParcel(
                source: Parcel,
                loader: ClassLoader?
            ): ProgressCopyOption = ProgressCopyOption(source, loader)

            override fun newArray(size: Int): Array<ProgressCopyOption?> = arrayOfNulls(size)
        }
    }

    @Parcelize
    private class ListenerArgs(val copiedSize: Long) : ParcelableArgs
}
