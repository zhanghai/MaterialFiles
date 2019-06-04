/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.os.Build;
import android.system.OsConstants;

import java.io.Closeable;
import java.io.FileDescriptor;

import androidx.annotation.NonNull;
import me.zhanghai.java.reflected.ReflectedMethod;

public class NioUtilsCompat {

    static {
        RestrictedHiddenApiAccess.allow();
    }

    @RestrictedHiddenApi
    private static final ReflectedMethod<?> sNewFileChannelMethod = new ReflectedMethod<>(
            "java.nio.NioUtils", "newFileChannel", Closeable.class, FileDescriptor.class,
            int.class);

    @RestrictedHiddenApi
    private static final ReflectedMethod<?> sFileChannelImplOpen = new ReflectedMethod<>(
            "sun.nio.ch.FileChannelImpl", "open", FileDescriptor.class, String.class,
            boolean.class, boolean.class, boolean.class, Object.class);

    private NioUtilsCompat() {}

    @NonNull
    public static java.nio.channels.FileChannel newFileChannel(@NonNull Closeable ioObject,
                                                               @NonNull FileDescriptor fd,
                                                               int mode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // They broke O_RDONLY by assuming it's non-zero since N, but in fact it is zero.
            // https://android.googlesource.com/platform/libcore/+/nougat-release/luni/src/main/java/java/nio/NioUtils.java#63
            boolean readable = (mode & OsConstants.O_ACCMODE) != OsConstants.O_WRONLY;
            boolean writable = (mode & OsConstants.O_ACCMODE) != OsConstants.O_RDONLY;
            boolean append = (mode & OsConstants.O_APPEND) == OsConstants.O_APPEND;
            return sFileChannelImplOpen.invoke(null, fd, null, readable, writable, append,
                    ioObject);
        } else {
            return sNewFileChannelMethod.invoke(null, ioObject, fd, mode);
        }
    }
}
