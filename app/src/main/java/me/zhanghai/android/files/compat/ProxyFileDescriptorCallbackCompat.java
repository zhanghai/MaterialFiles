/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.os.Build;
import android.os.ProxyFileDescriptorCallback;
import android.system.ErrnoException;
import android.system.OsConstants;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * @see ProxyFileDescriptorCallback
 */
public abstract class ProxyFileDescriptorCallbackCompat {

    /**
     * @see ProxyFileDescriptorCallback#onGetSize()
     */
    public long onGetSize() throws ErrnoException {
        throw new ErrnoException("onGetSize", OsConstants.EBADF);
    }

    /**
     * @see ProxyFileDescriptorCallback#onRead(long, int, byte[])
     */
    public int onRead(long offset, int size, byte[] data) throws ErrnoException {
        throw new ErrnoException("onRead", OsConstants.EBADF);
    }

    /**
     * @see ProxyFileDescriptorCallback#onWrite(long, int, byte[])
     */
    public int onWrite(long offset, int size, byte[] data) throws ErrnoException {
        throw new ErrnoException("onWrite", OsConstants.EBADF);
    }

    /**
     * @see ProxyFileDescriptorCallback#onFsync()
     */
    public void onFsync() throws ErrnoException {
        throw new ErrnoException("onFsync", OsConstants.EINVAL);
    }

    /**
     * @see ProxyFileDescriptorCallback#onRelease()
     */
    abstract public void onRelease();

    @NonNull
    @RequiresApi(Build.VERSION_CODES.O)
    ProxyFileDescriptorCallback toProxyFileDescriptorCallback() {
        return new ProxyFileDescriptorCallback() {
            @Override
            public long onGetSize() throws ErrnoException {
                return ProxyFileDescriptorCallbackCompat.this.onGetSize();
            }
            @Override
            public int onRead(long offset, int size, byte[] data) throws ErrnoException {
                return ProxyFileDescriptorCallbackCompat.this.onRead(offset, size, data);
            }
            @Override
            public int onWrite(long offset, int size, byte[] data) throws ErrnoException {
                return ProxyFileDescriptorCallbackCompat.this.onWrite(offset, size, data);
            }
            @Override
            public void onFsync() throws ErrnoException {
                ProxyFileDescriptorCallbackCompat.this.onFsync();
            }
            @Override
            public void onRelease() {
                ProxyFileDescriptorCallbackCompat.this.onRelease();
            }
        };
    }
}
