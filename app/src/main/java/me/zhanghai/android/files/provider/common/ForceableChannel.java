/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.util.Objects;

import androidx.annotation.NonNull;

public interface ForceableChannel {

    void force(boolean metaData) throws IOException;

    static boolean isForceable(@NonNull Channel channel) {
        Objects.requireNonNull(channel);
        return channel instanceof FileChannel || channel instanceof ForceableChannel;
    }

    static void force(@NonNull Channel channel, boolean metaData) throws IOException {
        Objects.requireNonNull(channel);
        if (channel instanceof FileChannel) {
            FileChannel fileChannel = (FileChannel) channel;
            fileChannel.force(metaData);
        } else if (channel instanceof ForceableChannel) {
            ForceableChannel forceableChannel = (ForceableChannel) channel;
            forceableChannel.force(metaData);
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
