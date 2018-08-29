/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.jni;

public class Linux {

    static {
        System.loadLibrary("linux");
    }

    public static native StructPasswd getpwnam(String name);

    public static native StructPasswd getpwuid(int uid);

    public static native StructGroup getgrnam(String name);

    public static native StructGroup getgrgid(int gid);
}
