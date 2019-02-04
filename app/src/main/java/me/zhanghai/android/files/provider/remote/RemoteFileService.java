/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.RemoteException;

import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.FileSystem;
import me.zhanghai.android.files.provider.common.PosixFileAttributeView;

public class RemoteFileService {

    @NonNull
    private static final RemoteFileService sInstance = new RemoteFileService();

    private Implementation mImplementation;
    @NonNull
    private final Object mImplementationLock = new Object();

    private RemoteFileService() {}

    @NonNull
    public static RemoteFileService getInstance() {
        return sInstance;
    }

    public static void use(@NonNull Implementation implementation) {
        Objects.requireNonNull(implementation);
        synchronized (sInstance.mImplementationLock) {
            sInstance.mImplementation = implementation;
        }
    }

    @NonNull
    public IRemoteFileSystemProvider getRemoteFileSystemProviderInterface(
            @NonNull String remoteScheme) throws RemoteFileSystemException {
        IRemoteFileService remoteInterface = mImplementation.getRemoteInterface();
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
        IRemoteFileService remoteInterface = mImplementation.getRemoteInterface();
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
    public IRemotePosixFileAttributeView getRemotePosixFileAttributeViewInterface(
            @NonNull PosixFileAttributeView attributeView) throws RemoteFileSystemException {
        ParcelableObject parcelableAttributeView = new ParcelableObject(attributeView);
        IRemoteFileService remoteInterface = mImplementation.getRemoteInterface();
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
        IRemoteFileService remoteInterface = mImplementation.getRemoteInterface();
        try {
            remoteInterface.refreshArchiveFileSystem(parcelableFileSystem);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
    }

    public interface Implementation {

        @NonNull
        IRemoteFileService getRemoteInterface() throws RemoteFileSystemException;
    }
}
