/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException

class RemoteCallback : Parcelable {
    private val localCallback: ((Bundle) -> Unit)?
    private val remoteCallback: IRemoteCallback?

    constructor(callback: (Bundle) -> Unit) {
        localCallback = callback
        remoteCallback = null
    }

    fun sendResult(result: Bundle) {
        if (remoteCallback != null) {
            try {
                remoteCallback.sendResult(result)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        } else {
            localCallback!!(result)
        }
    }

    private inner class Stub : IRemoteCallback.Stub() {
        override fun sendResult(result: Bundle) {
            this@RemoteCallback.sendResult(result)
        }
    }

    private constructor(source: Parcel) {
        localCallback = null
        remoteCallback = IRemoteCallback.Stub.asInterface(source.readStrongBinder())
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeStrongBinder(Stub().asBinder())
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<RemoteCallback> {
            override fun createFromParcel(source: Parcel): RemoteCallback = RemoteCallback(source)

            override fun newArray(size: Int): Array<RemoteCallback?> = arrayOfNulls(size)
        }
    }
}
