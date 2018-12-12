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
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;
import me.zhanghai.android.files.reflected.ReflectedAccessor;
import me.zhanghai.android.files.reflected.ReflectedClass;
import me.zhanghai.android.files.reflected.ReflectedClassMethod;
import me.zhanghai.android.files.reflected.RestrictedHiddenApi;

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

    private static class NioUtilsCompat {

        static {
            ReflectedAccessor.allowRestrictedHiddenApiAccess();
        }

        @RestrictedHiddenApi
        private static final ReflectedClassMethod sNewFileChannelMethod = new ReflectedClassMethod(
                new ReflectedClass("java.nio.NioUtils"), "newFileChannel", Closeable.class,
                FileDescriptor.class, int.class);

        private NioUtilsCompat() {}

        @NonNull
        public static java.nio.channels.FileChannel newFileChannel(@NonNull Closeable ioObject,
                                                                   @NonNull FileDescriptor fd,
                                                                   int mode) {
            return sNewFileChannelMethod.invoke(null, ioObject, fd, mode);
        }
    }
}
