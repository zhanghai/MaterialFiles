/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

import android.os.Build;
import android.system.OsConstants;

public class Constants {

    private Constants() {}

    public static final int O_DSYNC = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 ?
            OsConstants.O_DSYNC : 00010000;

    public static final long UTIME_NOW = (1L << 30) - 1L;

    public static final long UTIME_OMIT = (1L << 30) - 2L;
}
