/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.os.Build;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

@RequiresApi(Build.VERSION_CODES.N)
public class SeekableByteChannelCompat {

    private SeekableByteChannelCompat() {}

    @NonNull
    public static SeekableByteChannel from(
            @NonNull java8.nio.channels.SeekableByteChannel channel) {
        return new DelegateChannel(channel);
    }

    private static class DelegateChannel implements SeekableByteChannel {

        @NonNull
        private final java8.nio.channels.SeekableByteChannel mChannel;

        DelegateChannel(@NonNull java8.nio.channels.SeekableByteChannel channel) {
            mChannel = channel;
        }

        @Override
        public int read(@NonNull ByteBuffer dst) throws IOException {
            return mChannel.read(dst);
        }

        @Override
        public int write(@NonNull ByteBuffer src) throws IOException {
            return mChannel.write(src);
        }

        @Override
        public long position() throws IOException {
            return mChannel.position();
        }

        @NonNull
        @Override
        public SeekableByteChannel position(long newPosition) throws IOException {
            mChannel.position(newPosition);
            return this;
        }

        @Override
        public long size() throws IOException {
            return mChannel.size();
        }

        @NonNull
        @Override
        public SeekableByteChannel truncate(long size) throws IOException {
            mChannel.truncate(size);
            return this;
        }

        @Override
        public boolean isOpen() {
            return mChannel.isOpen();
        }

        @Override
        public void close() throws IOException {
            mChannel.close();
        }
    }
}
