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
    private final PosixFileAttributeView mAttributeView;

    @NonNull
    private final RemoteInterfaceHolder<IRemotePosixFileAttributeView> mRemoteInterface;

    public RemotePosixFileAttributeView(@NonNull PosixFileAttributeView attributeView) {
        mAttributeView = attributeView;

        mRemoteInterface = new RemoteInterfaceHolder<>(() -> RemoteFileService.getInstance()
                .getRemotePosixFileAttributeViewInterface(mAttributeView));
    }

    @NonNull
    @Override
    public FA readAttributes() throws IOException {
        ParcelableIoException ioException = new ParcelableIoException();
        IRemotePosixFileAttributeView remoteInterface = mRemoteInterface.get();
        ParcelableObject parcelableAttributes;
        try {
            parcelableAttributes = remoteInterface.readAttributes(ioException);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        ioException.throwIfNonNull();
        return parcelableAttributes.get();
    }

    @Override
    public void setTimes(@Nullable FileTime lastModifiedTime, @Nullable FileTime lastAccessTime,
                         @Nullable FileTime createTime) throws IOException {
        ParcelableFileTime parcelableLastModifiedTime = new ParcelableFileTime(lastModifiedTime);
        ParcelableFileTime parcelableLastAccessTime = new ParcelableFileTime(lastAccessTime);
        ParcelableFileTime parcelableCreateTime = new ParcelableFileTime(createTime);
        ParcelableIoException ioException = new ParcelableIoException();
        IRemotePosixFileAttributeView remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.setTimes(parcelableLastModifiedTime, parcelableLastAccessTime,
                    parcelableCreateTime, ioException);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        ioException.throwIfNonNull();
    }

    @Override
    public void setOwner(@NonNull PosixUser owner) throws IOException {
        ParcelableIoException ioException = new ParcelableIoException();
        IRemotePosixFileAttributeView remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.setOwner(owner, ioException);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        ioException.throwIfNonNull();
    }

    @Override
    public void setGroup(@NonNull PosixGroup group) throws IOException {
        ParcelableIoException ioException = new ParcelableIoException();
        IRemotePosixFileAttributeView remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.setGroup(group, ioException);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        ioException.throwIfNonNull();
    }

    public void setMode(@NonNull Set<PosixFileModeBit> mode) throws IOException {
        ParcelablePosixFileMode parcelableMode = new ParcelablePosixFileMode(mode);
        ParcelableIoException ioException = new ParcelableIoException();
        IRemotePosixFileAttributeView remoteInterface = mRemoteInterface.get();
        try {
            remoteInterface.setMode(parcelableMode, ioException);
        } catch (RemoteException e) {
            throw new RemoteFileSystemException(e);
        }
        ioException.throwIfNonNull();
    }
}
