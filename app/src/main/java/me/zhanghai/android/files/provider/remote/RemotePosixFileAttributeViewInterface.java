/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import java.io.IOException;
import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.file.attribute.FileTime;
import me.zhanghai.android.files.provider.common.ParcelableFileTime;
import me.zhanghai.android.files.provider.common.ParcelablePosixFileMode;
import me.zhanghai.android.files.provider.common.PosixFileAttributeView;
import me.zhanghai.android.files.provider.common.PosixFileAttributes;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixUser;

public class RemotePosixFileAttributeViewInterface extends IRemotePosixFileAttributeView.Stub {

    @NonNull
    private final PosixFileAttributeView mAttributeView;

    public RemotePosixFileAttributeViewInterface(@NonNull PosixFileAttributeView attributeView) {
        mAttributeView = attributeView;
    }

    @NonNull
    @Override
    public ParcelableFileAttributes readAttributes(@NonNull ParcelableIoException ioException) {
        PosixFileAttributes attributes;
        try {
            attributes = mAttributeView.readAttributes();
        } catch (IOException e) {
            ioException.set(e);
            return null;
        }
        return new ParcelableFileAttributes(attributes);
    }

    @Override
    public void setTimes(@NonNull ParcelableFileTime parcelableLastModifiedTime,
                         @NonNull ParcelableFileTime parcelableLastAccessTime,
                         @NonNull ParcelableFileTime parcelableCreateTime,
                         @NonNull ParcelableIoException ioException) {
        FileTime lastModifiedTime = parcelableLastModifiedTime.get();
        FileTime lastAccessTime = parcelableLastAccessTime.get();
        FileTime createTime = parcelableCreateTime.get();
        try {
            mAttributeView.setTimes(lastModifiedTime, lastAccessTime, createTime);
        } catch (IOException e) {
            ioException.set(e);
        }
    }

    @Override
    public void setOwner(@NonNull PosixUser owner, @NonNull ParcelableIoException ioException) {
        try {
            mAttributeView.setOwner(owner);
        } catch (IOException e) {
            ioException.set(e);
        }
    }

    @Override
    public void setGroup(@NonNull PosixGroup group, @NonNull ParcelableIoException ioException) {
        try {
            mAttributeView.setGroup(group);
        } catch (IOException e) {
            ioException.set(e);
        }
    }

    @Override
    public void setMode(@NonNull ParcelablePosixFileMode parcelableMode,
                        @NonNull ParcelableIoException ioException) {
        Set<PosixFileModeBit> mode = parcelableMode.get();
        try {
            mAttributeView.setMode(mode);
        } catch (IOException e) {
            ioException.set(e);
        }
    }
}
