package me.zhanghai.android.files.provider.remote;

import me.zhanghai.android.files.provider.remote.ParcelableIoException;

interface IRemoteFileSystem {

    void close(out ParcelableIoException ioException);
}
