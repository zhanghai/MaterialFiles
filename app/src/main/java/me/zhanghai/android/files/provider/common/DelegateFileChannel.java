/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.channels.FileChannel;

public class DelegateFileChannel extends FileChannel {

    @NonNull
    private final FileChannel mFileChannel;

    public DelegateFileChannel(@NonNull FileChannel fileChannel) {
        mFileChannel = fileChannel;
    }

    @Override
    public int read(@NonNull ByteBuffer dst) throws IOException {
        return mFileChannel.read(dst);
    }

    @Override
    public long read(@NonNull ByteBuffer[] dsts, int offset, int length) throws IOException {
        return mFileChannel.read(dsts, offset, length);
    }

    @Override
    public int write(@NonNull ByteBuffer src) throws IOException {
        return mFileChannel.write(src);
    }

    @Override
    public long write(@NonNull ByteBuffer[] srcs, int offset, int length) throws IOException {
        return mFileChannel.write(srcs, offset, length);
    }

    @Override
    public long position() throws IOException {
        return mFileChannel.position();
    }

    @NonNull
    @Override
    public DelegateFileChannel position(long newPosition) throws IOException {
        mFileChannel.position(newPosition);
        return this;
    }

    @Override
    public long size() throws IOException {
        return mFileChannel.size();
    }

    @NonNull
    @Override
    public DelegateFileChannel truncate(long size) throws IOException {
        mFileChannel.truncate(size);
        return this;
    }

    public void force(boolean metaData) throws IOException {
        mFileChannel.force(metaData);
    }

    public long transferTo(long position, long count, @NonNull WritableByteChannel target)
            throws IOException {
        return mFileChannel.transferTo(position, count, target);
    }

    public long transferFrom(@NonNull ReadableByteChannel src, long position, long count)
            throws IOException {
        return mFileChannel.transferFrom(src, position, count);
    }

    public int read(@NonNull ByteBuffer dst, long position) throws IOException {
        return mFileChannel.read(dst, position);
    }

    public int write(@NonNull ByteBuffer src, long position) throws IOException {
        return mFileChannel.write(src, position);
    }

    @NonNull
    public MappedByteBuffer map(@NonNull MapMode mode, long position, long size)
            throws IOException {
        return mFileChannel.map(mode, position, size);
    }

    @NonNull
    public FileLock lock(long position, long size, boolean shared) throws IOException {
        return mFileChannel.lock(position, size, shared);
    }

    @Nullable
    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        return mFileChannel.tryLock(position, size, shared);
    }

    @Override
    protected void implCloseChannel() throws IOException {
        mFileChannel.close();
    }
}
