/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import me.zhanghai.android.files.util.ParcelableArgs
import me.zhanghai.android.files.util.ParcelableListParceler
import me.zhanghai.android.files.util.RemoteCallback
import me.zhanghai.android.files.util.getArgs
import me.zhanghai.android.files.util.putArgs
import me.zhanghai.android.files.util.readParcelable

class ParcelablePathListConsumer(val value: (List<Path>) -> Unit) : Parcelable {
    private constructor(source: Parcel) : this(
        source.readParcelable<RemoteCallback>()!!.let<RemoteCallback, (List<Path>) -> Unit> {
            { paths -> it.sendResult(Bundle().putArgs(ListenerArgs(paths))) }
        }
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(RemoteCallback { value(it.getArgs<ListenerArgs>().paths) }, flags)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ParcelablePathListConsumer> {
            override fun createFromParcel(source: Parcel): ParcelablePathListConsumer =
                ParcelablePathListConsumer(source)

            override fun newArray(size: Int): Array<ParcelablePathListConsumer?> =
                arrayOfNulls(size)
        }
    }

    @Parcelize
    private class ListenerArgs(
        val paths: @WriteWith<ParcelableListParceler> List<Path>
    ) : ParcelableArgs
}

fun ((List<Path>) -> Unit).toParcelable(): ParcelablePathListConsumer =
    ParcelablePathListConsumer(this)
