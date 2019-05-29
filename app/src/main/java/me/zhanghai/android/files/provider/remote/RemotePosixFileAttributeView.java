/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.RemoteException;

import java.io.IOException;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.attribute.FileTime;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.ParcelableFileTime;
import me.zhanghai.android.files.provider.common.ParcelablePosixFileMode;
import me.zhanghai.android.files.provider.common.PosixFileAttributeView;
import me.zhanghai.android.files.provider.common.PosixFileAttributes;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixUser;

public abstract class RemotePosixFileAttributeView<FA extends PosixFileAttributes>
        implements PosixFileAttributeView {

    @NonNull
    private final RemoteInterfaceHolder<IRemotePosixFileAttributeView> mRemoteInterface;

    public RemotePosixFileAttributeView(
            @NonNull RemoteInterfaceHolder<IRemotePosixFileAttributeView> remoteInterface) {
        mRemoteInterface = remoteInterface;
    }

    @NonNull
    @Override
    public FA readAttributes() throws IOException {
        ParcelableException exception = new ParcelableException();
        IRemotePosixFileAttributeView remoteInterface = mRemoteInterface.get();
        ParcelableObject parcelableAttributes;
        try {
            parcelableAttributes = remoteInterface.readAttributes(exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
        return parcelableAttributes.get();
    }

    @Override
    public void setTimes(@Nullable FileTime lastModifiedTime, @Nullable FileTime lastAccessTime,
                         @Nullable FileTime createTime) throws IOException {
        ParcelableFileTime parcelableLastModifiedTime = new ParcelableFileTime(lastModifiedTime);
        ParcelableFileTime parcelableLastAccessTime = new ParcelableFileTime(lastAccessTime);
        ParcelableFileTime parcelableCreateTime = new ParcelableFileTime(createTime);
        ParcelableException exception = new ParcelableException();
        IRemotePosixFileAttributeView remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.setTimes(parcelableLastModifiedTime, parcelableLastAccessTime,
                    parcelableCreateTime, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
    }

    @Override
    public void setOwner(@NonNull PosixUser owner) throws IOException {
        ParcelableException exception = new ParcelableException();
        IRemotePosixFileAttributeView remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.setOwner(owner, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
    }

    @Override
    public void setGroup(@NonNull PosixGroup group) throws IOException {
        ParcelableException exception = new ParcelableException();
        IRemotePosixFileAttributeView remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.setGroup(group, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
    }

    public void setMode(@NonNull Set<PosixFileModeBit> mode) throws IOException {
        ParcelablePosixFileMode parcelableMode = new ParcelablePosixFileMode(mode);
        ParcelableException exception = new ParcelableException();
        IRemotePosixFileAttributeView remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.setMode(parcelableMode, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
    }

    public void setSeLinuxContext(@NonNull ByteString context) throws IOException {
        ParcelableObject parcelableContext = new ParcelableObject(context);
        ParcelableException exception = new ParcelableException();
        IRemotePosixFileAttributeView remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.setSeLinuxContext(parcelableContext, exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
    }

    public void restoreSeLinuxContext() throws IOException {
        ParcelableException exception = new ParcelableException();
        IRemotePosixFileAttributeView remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.restoreSeLinuxContext(exception);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        exception.throwIfNotNull();
    }
}
