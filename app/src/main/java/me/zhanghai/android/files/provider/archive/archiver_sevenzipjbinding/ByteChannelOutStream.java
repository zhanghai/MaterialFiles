/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver_sevenzipjbinding;

import net.sf.sevenzipjbinding.IOutStream;
import net.sf.sevenzipjbinding.SevenZipException;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import java8.nio.channels.SeekableByteChannel;

public class ByteChannelOutStream implements Closeable, IOutStream {

    @NonNull
    private final SeekableByteChannel mChannel;

    public ByteChannelOutStream(@NonNull SeekableByteChannel channel) {
        mChannel = channel;
    }

    @Override
    public synchronized long seek(long offset, int seekOrigin) throws SevenZipException {
        try {
            switch (seekOrigin) {
                case SEEK_SET:
                    mChannel.position(offset);
                    break;
                case SEEK_CUR:
                    mChannel.position(mChannel.position() + offset);
                    break;
                case SEEK_END:
                    mChannel.position(mChannel.size() + offset);
                    break;
                default:
                    throw new AssertionError(seekOrigin);
            }
            return mChannel.position();
        } catch (IOException e) {
            throw new SevenZipException(e);
        }
    }

    @Override
    public synchronized void setSize(long newSize) throws SevenZipException {
        try {
            if (newSize <= mChannel.size()) {
                mChannel.truncate(newSize);
            } else {
                long oldPosition = mChannel.position();
                mChannel.position(newSize - 1);
                try {
                    mChannel.write(ByteBuffer.wrap(new byte[] {0}));
                } finally {
                    mChannel.position(oldPosition);
                }
            }
        } catch (IOException e) {
            throw new SevenZipException(e);
        }
    }

    @Override
    public synchronized int write(@NonNull byte[] data) throws SevenZipException {
        try {
            return mChannel.write(ByteBuffer.wrap(data));
        } catch (IOException e) {
            throw new SevenZipException(e);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        mChannel.close();
    }
}
