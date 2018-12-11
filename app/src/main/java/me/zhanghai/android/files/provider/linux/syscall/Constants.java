/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

public class Constants {

    private Constants() {}

    public static final long UTIME_NOW = (1L << 30) - 1L;

    public static final long UTIME_OMIT = (1L << 30) - 2L;
}
