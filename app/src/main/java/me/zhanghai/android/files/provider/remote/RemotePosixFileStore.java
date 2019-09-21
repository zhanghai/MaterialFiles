/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.RemoteException;

import java.io.IOException;

import androidx.annotation.NonNull;
import java8.nio.file.attribute.FileAttributeView;
import me.zhanghai.android.files.provider.common.PosixFileStore;

public abstract class RemotePosixFileStore extends PosixFileStore {

    @NonNull
    private final RemoteInterfaceHolder<IRemotePosixFileStore> mRemoteInterface;

    public RemotePosixFileStore(
            @NonNull RemoteInterfaceHolder<IRemotePosixFileStore> remoteInterface) {
        mRemoteInterface = remoteInterface;
    }

    @Override
    public void refresh() {
        throw new AssertionError();
    }

    @NonNull
    @Override
    public String name() {
        throw new AssertionError();
    }

    @NonNull
    @Override
    public String type() {
        throw new AssertionError();
    }

    @Override
    public boolean isReadOnly() {
        throw new AssertionError();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws IOException {
        ParcelableException exception = new ParcelableException();
        try {
            mRemoteInterface.get().setReadOnly(readOnly, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
    }

    @Override
    public long getTotalSpace() throws IOException {
        ParcelableException exception = new ParcelableException();
        long totalSpace;
        try {
            totalSpace = mRemoteInterface.get().getTotalSpace(exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
        return totalSpace;
    }

    @Override
    public long getUsableSpace() throws IOException {
        ParcelableException exception = new ParcelableException();
        long usableSpace;
        try {
            usableSpace = mRemoteInterface.get().getUsableSpace(exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
        return usableSpace;
    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        ParcelableException exception = new ParcelableException();
        long unallocatedSpace;
        try {
            unallocatedSpace = mRemoteInterface.get().getUnallocatedSpace(exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
        return unallocatedSpace;
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull Class<? extends FileAttributeView> type) {
        throw new AssertionError();
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull String name) {
        throw new AssertionError();
    }
}
