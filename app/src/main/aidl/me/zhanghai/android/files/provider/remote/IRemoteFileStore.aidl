package me.zhanghai.android.files.provider.remote;

import me.zhanghai.android.files.provider.remote.ParcelableException;

interface IRemoteFileStore {

    long getTotalSpace(out ParcelableException exception);

    long getUsableSpace(out ParcelableException exception);

    long getUnallocatedSpace(out ParcelableException exception);
}
