/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.RemoteException
import me.zhanghai.android.files.provider.common.PathObservable
import me.zhanghai.android.files.util.RemoteCallback
import java.io.IOException

class RemotePathObservable : PathObservable, Parcelable {
    private val localPathObservable: PathObservable?
    private val remotePathObservable: IRemotePathObservable?
    private val remoteObservers: MutableSet<() -> Unit>?
    private var isRemoteInitialized = false
    private val remoteLock: Any?

    constructor(pathObservable: PathObservable) {
        localPathObservable = pathObservable
        remotePathObservable = null
        remoteObservers = null
        remoteLock = null
    }

    @Throws(IOException::class)
    fun initializeForRemote() {
        synchronized(remoteLock!!) {
            check(!isRemoteInitialized)
            try {
                remotePathObservable!!.addObserver(RemoteCallback {
                    synchronized(remoteLock) { remoteObservers!!.forEach { it() } }
                })
            } catch (e: RemoteException) {
                close()
                throw RemoteFileSystemException(e)
            }
            isRemoteInitialized = true
        }
    }

    override fun addObserver(observer: () -> Unit) {
        synchronized(remoteLock!!) {
            check(isRemoteInitialized)
            remoteObservers!!.add(observer)
        }
    }

    override fun removeObserver(observer: () -> Unit) {
        synchronized(remoteLock!!) {
            check(isRemoteInitialized)
            remoteObservers!!.remove(observer)
        }
    }

    @Throws(IOException::class)
    override fun close() {
        if (remotePathObservable != null) {
            synchronized(remoteLock!!) {
                remotePathObservable.call { exception -> close(exception) }
                remoteObservers!!.clear()
            }
        } else {
            localPathObservable!!.close()
        }
    }

    private class Stub(private val pathObservable: PathObservable) : IRemotePathObservable.Stub() {
        override fun addObserver(observer: RemoteCallback) {
            pathObservable.addObserver { observer.sendResult(Bundle()) }
        }

        override fun close(exception: ParcelableException) {
            tryRun(exception) { pathObservable.close() }
        }
    }

    private constructor(source: Parcel) {
        localPathObservable = null
        remotePathObservable = IRemotePathObservable.Stub.asInterface(source.readStrongBinder())
        remoteObservers = mutableSetOf()
        remoteLock = Any()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        check(remotePathObservable == null) { "Already at the remote side" }
        dest.writeStrongBinder(Stub(localPathObservable!!).asBinder())
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<RemotePathObservable> {
            override fun createFromParcel(source: Parcel): RemotePathObservable =
                RemotePathObservable(source)

            override fun newArray(size: Int): Array<RemotePathObservable?> = arrayOfNulls(size)
        }
    }
}

fun PathObservable.toRemote(): RemotePathObservable = RemotePathObservable(this)
