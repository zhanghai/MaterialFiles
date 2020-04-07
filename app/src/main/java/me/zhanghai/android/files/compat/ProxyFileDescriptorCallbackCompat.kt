/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.os.Build
import android.os.ProxyFileDescriptorCallback
import android.system.ErrnoException
import android.system.OsConstants
import androidx.annotation.RequiresApi

abstract class ProxyFileDescriptorCallbackCompat {
    @Throws(ErrnoException::class)
    open fun onGetSize(): Long {
        throw ErrnoException("onGetSize", OsConstants.EBADF)
    }

    @Throws(ErrnoException::class)
    open fun onRead(offset: Long, size: Int, data: ByteArray): Int {
        throw ErrnoException("onRead", OsConstants.EBADF)
    }

    @Throws(ErrnoException::class)
    open fun onWrite(offset: Long, size: Int, data: ByteArray): Int {
        throw ErrnoException("onWrite", OsConstants.EBADF)
    }

    @Throws(ErrnoException::class)
    open fun onFsync() {
        throw ErrnoException("onFsync", OsConstants.EINVAL)
    }

    abstract fun onRelease()

    @RequiresApi(Build.VERSION_CODES.O)
    fun toProxyFileDescriptorCallback(): ProxyFileDescriptorCallback {
        return object : ProxyFileDescriptorCallback() {
            @Throws(ErrnoException::class)
            override fun onGetSize(): Long = this@ProxyFileDescriptorCallbackCompat.onGetSize()

            @Throws(ErrnoException::class)
            override fun onRead(offset: Long, size: Int, data: ByteArray): Int =
                this@ProxyFileDescriptorCallbackCompat.onRead(offset, size, data)

            @Throws(ErrnoException::class)
            override fun onWrite(offset: Long, size: Int, data: ByteArray): Int =
                this@ProxyFileDescriptorCallbackCompat.onWrite(offset, size, data)

            @Throws(ErrnoException::class)
            override fun onFsync() = this@ProxyFileDescriptorCallbackCompat.onFsync()

            override fun onRelease() = this@ProxyFileDescriptorCallbackCompat.onRelease()
        }
    }
}
