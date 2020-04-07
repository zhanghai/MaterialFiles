package me.zhanghai.android.files.provider.remote;

import me.zhanghai.android.files.provider.remote.ParcelableException;

interface IRemoteFileSystem {
    void close(out ParcelableException exception);
}
