/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.RemoteException;

import java.io.IOException;

import androidx.annotation.NonNull;
import java8.nio.file.FileSystem;

public abstract class RemoteFileSystem extends FileSystem {

    @NonNull
    private final RemoteInterfaceHolder<IRemoteFileSystem> mRemoteInterface;

    public RemoteFileSystem(@NonNull RemoteInterfaceHolder<IRemoteFileSystem> remoteInterface) {
        mRemoteInterface = remoteInterface;
    }

    @Override
    public void close() throws IOException {
        if (!mRemoteInterface.has()) {
            return;
        }
        ParcelableException exception = new ParcelableException();
        IRemoteFileSystem remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.close(exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
    }

    protected boolean hasRemoteInterface() {
        return mRemoteInterface.has();
    }

    public void ensureRemoteInterface() throws RemoteFileSystemException {
        mRemoteInterface.get();
    }
}
