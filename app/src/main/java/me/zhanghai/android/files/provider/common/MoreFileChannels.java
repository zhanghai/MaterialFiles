/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.os.ParcelFileDescriptor;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;

import androidx.annotation.NonNull;
import java8.nio.channels.FileChannel;
import java8.nio.channels.FileChannels;
import me.zhanghai.android.files.compat.NioUtilsCompat;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;

public class MoreFileChannels {

    private MoreFileChannels() {}

    @NonNull
    public static FileChannel open(@NonNull FileDescriptor fd, int flags) {
        Closeable closeable = new FileDescriptorCloseable(fd);
        return FileChannels.from(NioUtilsCompat.newFileChannel(closeable, fd, flags));
    }

    @NonNull
    public static FileChannel open(@NonNull ParcelFileDescriptor pfd, @NonNull String mode) {
        return FileChannels.from(NioUtilsCompat.newFileChannel(pfd, pfd.getFileDescriptor(),
                ParcelFileDescriptor.parseMode(mode)));
    }

    private static class FileDescriptorCloseable implements Closeable {

        @NonNull
        private final FileDescriptor mFd;

        public FileDescriptorCloseable(@NonNull FileDescriptor fd) {
            mFd = fd;
        }

        @Override
        public void close() throws IOException {
            try {
                Syscalls.close(mFd);
            } catch (SyscallException e) {
                throw new IOException(e);
            }
        }
    }
}
