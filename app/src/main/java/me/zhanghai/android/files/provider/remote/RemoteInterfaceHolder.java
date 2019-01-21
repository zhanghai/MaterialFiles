/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import androidx.annotation.NonNull;

public class RemoteInterfaceHolder<I extends IInterface> {

    @NonNull
    private final Getter<I> mGetter;

    private I mRemoteInterface;
    @NonNull
    private final Object mRemoteInterfaceLock = new Object();

    private final IBinder.DeathRecipient mBinderDied = this::binderDied;

    public RemoteInterfaceHolder(@NonNull Getter<I> getter) {
        mGetter = getter;
    }

    public I get() throws RemoteFileSystemException {
        synchronized (mRemoteInterfaceLock) {
            if (mRemoteInterface == null) {
                mRemoteInterface = mGetter.getRemoteInterface();
            }
            try {
                mRemoteInterface.asBinder().linkToDeath(mBinderDied, 0);
            } catch (RemoteException e) {
                // RemoteException is thrown if remote has already died.
                mRemoteInterface = null;
                throw new RemoteFileSystemException(e);
            }
            return mRemoteInterface;
        }
    }

    private void binderDied() {
        synchronized (mRemoteInterfaceLock) {
            mRemoteInterface = null;
        }
    }

    public interface Getter<I extends IInterface> {

        @NonNull
        I getRemoteInterface() throws RemoteFileSystemException;
    }
}
