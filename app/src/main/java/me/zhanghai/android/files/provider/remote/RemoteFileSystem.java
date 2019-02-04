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
    private final FileSystem mFileSystem;

    @NonNull
    private final RemoteInterfaceHolder<IRemoteFileSystem> mRemoteInterface;

    public RemoteFileSystem(@NonNull FileSystem fileSystem) {
        mFileSystem = fileSystem;
        mRemoteInterface = new RemoteInterfaceHolder<>(() -> RemoteFileService.getInstance()
                .getRemoteFileSystemInterface(mFileSystem));
    }

    @NonNull
    protected FileSystem getFileSystem() {
        return mFileSystem;
    }

    @Override
    public void close() throws IOException {
        if (!mRemoteInterface.has()) {
            return;
        }
        ParcelableIoException ioException = new ParcelableIoException();
        IRemoteFileSystem remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.close(ioException);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        ioException.throwIfNonNull();
    }

    protected boolean hasRemoteInterface() {
        return mRemoteInterface.has();
    }

    public void ensureRemoteInterface() throws RemoteFileSystemException {
        mRemoteInterface.get();
    }
}
