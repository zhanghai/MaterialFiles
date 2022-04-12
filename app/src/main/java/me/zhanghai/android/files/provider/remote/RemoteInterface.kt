/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote

import android.os.IBinder.DeathRecipient
import android.os.IInterface
import android.os.RemoteException
import java.lang.ref.WeakReference

class RemoteInterface<T : IInterface>(
    // @Throws(RemoteFileSystemException::class)
    private val creator: () -> T
) {
    private var value: T? = null

    private val lock = Any()

    private val deathRecipient = WeakDeathRecipient(this)

    fun has(): Boolean = synchronized(lock) { value != null }

    @Throws(RemoteFileSystemException::class)
    fun get(): T {
        synchronized(lock) {
            var value = value
            if (value == null) {
                value = creator()
                this.value = value
                try {
                    value.asBinder().linkToDeath(deathRecipient, 0)
                } catch (e: RemoteException) {
                    // RemoteException is thrown if remote has already died.
                    this.value = null
                    throw RemoteFileSystemException(e)
                }
            }
            return value
        }
    }

    private fun binderDied() {
        synchronized(lock) {
            value!!.asBinder().unlinkToDeath(deathRecipient, 0)
            value = null
        }
    }

    protected fun finalize() {
        if (value != null) {
            // We have to do this or JavaBinder will complain about BinderProxy being destroyed
            // without unlinkToDeath() being called first.
            value!!.asBinder().unlinkToDeath(deathRecipient, 0)
            value = null
        }
    }

    // Avoid a strong reference to the BinderProxy so that it can be garbage collected.
    private class WeakDeathRecipient<T : IInterface>(
        remoteInterface: RemoteInterface<T>
    ) : DeathRecipient {
        private val weakRemoteInterface = WeakReference(remoteInterface)

        override fun binderDied() {
            weakRemoteInterface.get()?.binderDied()
        }
    }
}
