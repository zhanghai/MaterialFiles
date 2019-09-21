/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import java.io.IOException;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.provider.common.PosixFileStore;

public class RemotePosixFileStoreInterface extends IRemotePosixFileStore.Stub {

    @NonNull
    private final PosixFileStore mFileStore;

    public RemotePosixFileStoreInterface(@NonNull PosixFileStore fileStore) {
        mFileStore = fileStore;
    }

    @Override
    public void setReadOnly(boolean readOnly, @NonNull ParcelableException exception) {
        try {
            mFileStore.setReadOnly(readOnly);
        } catch (IOException | RuntimeException e) {
            exception.set(e);
        }
    }

    @Override
    public long getTotalSpace(@NonNull ParcelableException exception) {
        long totalSpace;
        try {
            totalSpace = mFileStore.getTotalSpace();
        } catch (IOException | RuntimeException e) {
            exception.set(e);
            return 0;
        }
        return totalSpace;
    }

    @Override
    public long getUsableSpace(@NonNull ParcelableException exception) {
        long usableSpace;
        try {
            usableSpace = mFileStore.getUsableSpace();
        } catch (IOException | RuntimeException e) {
            exception.set(e);
            return 0;
        }
        return usableSpace;
    }

    @Override
    public long getUnallocatedSpace(@NonNull ParcelableException exception) {
        long unallocatedSpace;
        try {
            unallocatedSpace = mFileStore.getUnallocatedSpace();
        } catch (IOException | RuntimeException e) {
            exception.set(e);
            return 0;
        }
        return unallocatedSpace;
    }
}
