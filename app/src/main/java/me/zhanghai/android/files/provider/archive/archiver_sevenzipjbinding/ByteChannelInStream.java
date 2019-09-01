/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver_sevenzipjbinding;

import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.SevenZipException;

import java.io.IOException;
import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import java8.nio.channels.SeekableByteChannel;

public class ByteChannelInStream implements IInStream {

    @NonNull
    private final SeekableByteChannel mChannel;

    public ByteChannelInStream(@NonNull SeekableByteChannel channel) {
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
    public synchronized int read(@NonNull byte[] data) throws SevenZipException {
        try {
            return mChannel.read(ByteBuffer.wrap(data));
        } catch (IOException e) {
            throw new SevenZipException(e);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        mChannel.close();
    }
}
