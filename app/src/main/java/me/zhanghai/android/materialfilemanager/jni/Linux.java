/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.jni;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.system.ErrnoException;
import android.system.Int64Ref;

import java.io.FileDescriptor;

public class Linux {

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

    public static native long sendfile(@NonNull FileDescriptor outFd, @NonNull FileDescriptor inFd,
                                       Int64Ref offset, long count) throws ErrnoException;
}
