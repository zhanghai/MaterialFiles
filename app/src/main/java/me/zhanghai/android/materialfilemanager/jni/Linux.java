/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.jni;

import android.system.ErrnoException;

public class Linux {

    static {
        System.loadLibrary("linux");
    }

    public static native StructPasswd getpwnam(String name) throws ErrnoException;

    public static native StructPasswd getpwuid(int uid) throws ErrnoException;

    public static native StructGroup getgrnam(String name) throws ErrnoException;

    public static native StructGroup getgrgid(int gid) throws ErrnoException;
}
