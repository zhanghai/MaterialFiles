package me.zhanghai.android.files.provider.remote;

import me.zhanghai.android.files.provider.remote.ParcelableIoException;

interface IRemoteFileStore {

    long getTotalSpace(out ParcelableIoException ioException);

    long getUsableSpace(out ParcelableIoException ioException);

    long getUnallocatedSpace(out ParcelableIoException ioException);
}
