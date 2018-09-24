/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.jni;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.system.ErrnoException;
import android.system.Int64Ref;

import java.io.FileDescriptor;

public class Linux {

    public static final long UTIME_NOW = (1L << 30) - 1L;
    public static final long UTIME_OMIT = (1L << 30) - 2L;

    static {
        System.loadLibrary("linux");
    }

    @Nullable
    public static native StructPasswd getpwnam(@NonNull String name) throws ErrnoException;

    @Nullable
    public static native StructPasswd getpwuid(int uid) throws ErrnoException;

    @Nullable
    public static native StructGroup getgrnam(@NonNull String name) throws ErrnoException;

    @Nullable
    public static native StructGroup getgrgid(int gid) throws ErrnoException;

    @Nullable
    public static native byte[] lgetxattr(@NonNull String path, @NonNull String name)
            throws ErrnoException;

    @Nullable
    public static native String[] llistxattr(@NonNull String path) throws ErrnoException;

    public static native void lsetxattr(@NonNull String path, @NonNull String name,
                                        @NonNull byte[] value, int flags) throws ErrnoException;

    @Nullable
    public static native StructStatCompat lstat(@NonNull String path) throws ErrnoException;

    //public static native void lutimens(@NonNull String path,
    //                                   @NonNull @Size(2) StructTimespec[] times)
    public static native void lutimens(@NonNull String path,
                                       @NonNull @Size(2) StructTimespecCompat[] times)
            throws ErrnoException;

    public static native long sendfile(@NonNull FileDescriptor outFd, @NonNull FileDescriptor inFd,
                                       @Nullable Int64Ref offset, long count) throws ErrnoException;

    @Nullable
    public static native StructStatCompat stat(@NonNull String path) throws ErrnoException;
}
