package me.zhanghai.android.files.provider.remote;

import me.zhanghai.android.files.provider.remote.ParcelableException;

interface IRemoteSeekableByteChannel {
    int read(out byte[] destination, out ParcelableException exception);

    int write(in byte[] source, out ParcelableException exception);

    long position(out ParcelableException exception);

    void position2(long newPosition, out ParcelableException exception);

    long size(out ParcelableException exception);

    void truncate(long size, out ParcelableException exception);

    void force(boolean metaData, out ParcelableException exception);

    void close(out ParcelableException exception);
}
