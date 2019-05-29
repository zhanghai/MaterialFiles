/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import java.io.IOException;
import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.file.attribute.FileTime;
import me.zhanghai.android.files.provider.common.ByteString;
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
    public ParcelableObject readAttributes(@NonNull ParcelableException exception) {
        PosixFileAttributes attributes;
        try {
            attributes = mAttributeView.readAttributes();
        } catch (IOException | RuntimeException e) {
            exception.set(e);
            return null;
        }
        return new ParcelableObject(attributes);
    }

    @Override
    public void setTimes(@NonNull ParcelableFileTime parcelableLastModifiedTime,
                         @NonNull ParcelableFileTime parcelableLastAccessTime,
                         @NonNull ParcelableFileTime parcelableCreateTime,
                         @NonNull ParcelableException exception) {
        FileTime lastModifiedTime = parcelableLastModifiedTime.get();
        FileTime lastAccessTime = parcelableLastAccessTime.get();
        FileTime createTime = parcelableCreateTime.get();
        try {
            mAttributeView.setTimes(lastModifiedTime, lastAccessTime, createTime);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
        }
    }

    @Override
    public void setOwner(@NonNull PosixUser owner, @NonNull ParcelableException exception) {
        try {
            mAttributeView.setOwner(owner);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
        }
    }

    @Override
    public void setGroup(@NonNull PosixGroup group, @NonNull ParcelableException exception) {
        try {
            mAttributeView.setGroup(group);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
        }
    }

    @Override
    public void setMode(@NonNull ParcelablePosixFileMode parcelableMode,
                        @NonNull ParcelableException exception) {
        Set<PosixFileModeBit> mode = parcelableMode.get();
        try {
            mAttributeView.setMode(mode);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
        }
    }

    @Override
    public void setSeLinuxContext(@NonNull ParcelableObject parcelableContext,
                                  @NonNull ParcelableException exception) {
        ByteString context = parcelableContext.get();
        try {
            mAttributeView.setSeLinuxContext(context);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
        }
    }

    @Override
    public void restoreSeLinuxContext(@NonNull ParcelableException exception) {
        try {
            mAttributeView.restoreSeLinuxContext();
        } catch (IOException | RuntimeException e) {
            exception.set(e);
        }
    }
}
