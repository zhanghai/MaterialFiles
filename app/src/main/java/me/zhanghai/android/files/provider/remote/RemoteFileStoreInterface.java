/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import java.io.IOException;

import androidx.annotation.NonNull;
import java8.nio.file.FileStore;

public class RemoteFileStoreInterface extends IRemoteFileStore.Stub {

    @NonNull
    private final FileStore mFileStore;

    public RemoteFileStoreInterface(@NonNull FileStore fileStore) {
        mFileStore = fileStore;
    }

    @Override
    public long getTotalSpace(@NonNull ParcelableIoException ioException) {
        long totalSpace;
        try {
            totalSpace = mFileStore.getTotalSpace();
        } catch (IOException e) {
            ioException.set(e);
            return 0;
        }
        return totalSpace;
    }

    @Override
    public long getUsableSpace(@NonNull ParcelableIoException ioException) {
        long usableSpace;
        try {
            usableSpace = mFileStore.getUsableSpace();
        } catch (IOException e) {
            ioException.set(e);
            return 0;
        }
        return usableSpace;
    }

    @Override
    public long getUnallocatedSpace(@NonNull ParcelableIoException ioException) {
        long unallocatedSpace;
        try {
            unallocatedSpace = mFileStore.getUnallocatedSpace();
        } catch (IOException e) {
            ioException.set(e);
            return 0;
        }
        return unallocatedSpace;
    }
}
