/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcelable;
import android.os.RemoteException;

import java.io.IOException;

import androidx.annotation.NonNull;
import java8.nio.file.FileSystem;
import java8.nio.file.WatchService;
import java8.nio.file.spi.FileSystemProvider;

public abstract class RemoteFileSystem extends FileSystem implements Parcelable {

    @NonNull
    private final RemoteFileSystemProvider mProvider;

    private boolean mClosed;
    @NonNull
    private final Object mCloseLock = new Object();

    private RemoteInterfaceHolder<IRemoteFileSystem> mRemoteInterface = new RemoteInterfaceHolder<>(
            () -> RemoteFileService.getInstance().getRemoteFileSystemInterface(this));

    public RemoteFileSystem(@NonNull RemoteFileSystemProvider provider) {
        mProvider = provider;
    }

    @NonNull
    @Override
    public FileSystemProvider provider() {
        return mProvider;
    }

    @Override
    public void close() throws IOException {
        synchronized (mCloseLock) {
            if (mClosed) {
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
            mProvider.removeFileSystem(this);
            mClosed = true;
        }
    }

    @Override
    public boolean isOpen() {
        synchronized (mCloseLock) {
            return !mClosed;
        }
    }

    @NonNull
    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void ensureRemoteInterface() throws RemoteFileSystemException {
        mRemoteInterface.get();
    }
}
