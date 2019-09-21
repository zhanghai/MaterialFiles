/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.RemoteException;

import androidx.annotation.NonNull;
import java8.nio.file.FileSystem;
import me.zhanghai.android.files.provider.common.PosixFileAttributeView;
import me.zhanghai.android.files.provider.common.PosixFileStore;

public abstract class RemoteFileService {

    @NonNull
    private final RemoteInterfaceHolder<IRemoteFileService> mRemoteInterface;

    public RemoteFileService(@NonNull RemoteInterfaceHolder<IRemoteFileService> remoteInterface) {
        mRemoteInterface = remoteInterface;
    }

    @NonNull
    public IRemoteFileSystemProvider getRemoteFileSystemProviderInterface(
            @NonNull String remoteScheme) throws RemoteFileSystemException {
        IRemoteFileService remoteInterface = mRemoteInterface.get();
        IRemoteFileSystemProvider remoteProviderInterface;
        try {
            remoteProviderInterface = remoteInterface.getRemoteFileSystemProviderInterface(
                    remoteScheme);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        return remoteProviderInterface;
    }

    @NonNull
    public IRemoteFileSystem getRemoteFileSystemInterface(@NonNull FileSystem fileSystem)
            throws RemoteFileSystemException {
        ParcelableObject parcelableFileSystem = new ParcelableObject(fileSystem);
        IRemoteFileService remoteInterface = mRemoteInterface.get();
        IRemoteFileSystem remoteFileSystemInterface;
        try {
            remoteFileSystemInterface = remoteInterface.getRemoteFileSystemInterface(
                    parcelableFileSystem);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        return remoteFileSystemInterface;
    }

    @NonNull
    public IRemotePosixFileStore getRemotePosixFileStoreInterface(@NonNull PosixFileStore fileStore)
            throws RemoteFileSystemException {
        ParcelableObject parcelableFileStore = new ParcelableObject(fileStore);
        IRemoteFileService remoteInterface = mRemoteInterface.get();
        IRemotePosixFileStore remoteFileStoreInterface;
        try {
            remoteFileStoreInterface = remoteInterface.getRemotePosixFileStoreInterface(
                    parcelableFileStore);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        return remoteFileStoreInterface;
    }

    @NonNull
    public IRemotePosixFileAttributeView getRemotePosixFileAttributeViewInterface(
            @NonNull PosixFileAttributeView attributeView) throws RemoteFileSystemException {
        ParcelableObject parcelableAttributeView = new ParcelableObject(attributeView);
        IRemoteFileService remoteInterface = mRemoteInterface.get();
        IRemotePosixFileAttributeView remoteAttributeViewInterface;
        try {
            remoteAttributeViewInterface = remoteInterface.getRemotePosixFileAttributeViewInterface(
                    parcelableAttributeView);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        return remoteAttributeViewInterface;
    }

    public void refreshArchiveFileSystem(@NonNull FileSystem fileSystem)
            throws RemoteFileSystemException {
        ParcelableObject parcelableFileSystem = new ParcelableObject(fileSystem);
        IRemoteFileService remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.refreshArchiveFileSystem(parcelableFileSystem);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
    }
}
