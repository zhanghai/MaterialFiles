/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;

import androidx.annotation.NonNull;
import java8.nio.channels.FileChannel;
import java8.nio.channels.FileChannels;
import me.zhanghai.android.files.compat.NioUtilsCompat;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;

class LinuxFileChannels {

    private LinuxFileChannels() {}

    @NonNull
    public static FileChannel open(@NonNull FileDescriptor fd, int flags) {
        Closeable closeable = new FileDescriptorCloseable(fd);
        return FileChannels.from(NioUtilsCompat.newFileChannel(closeable, fd, flags));
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
