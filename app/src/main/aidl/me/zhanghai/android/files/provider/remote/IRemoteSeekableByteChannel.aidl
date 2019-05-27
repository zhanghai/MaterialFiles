package me.zhanghai.android.files.provider.remote;

import me.zhanghai.android.files.provider.remote.ParcelableException;

interface IRemoteSeekableByteChannel {

    int read(int remaining, out ParcelableException exception);

    int write(int remaining, out ParcelableException exception);

    long position(out ParcelableException exception);

    void position2(long newPosition, out ParcelableException exception);

    long size(out ParcelableException exception);

    void truncate(long size, out ParcelableException exception);

    void close(out ParcelableException exception);
}
