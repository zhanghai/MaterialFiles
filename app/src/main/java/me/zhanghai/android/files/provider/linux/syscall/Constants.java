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

    /*
     * @see android.os.SELinux#SELINUX_ANDROID_RESTORECON_NOCHANGE
     */
    public static final int SELINUX_ANDROID_RESTORECON_NOCHANGE = 1;

    /*
     * @see android.os.SELinux#SELINUX_ANDROID_RESTORECON_VERBOSE
     */
    public static final int SELINUX_ANDROID_RESTORECON_VERBOSE = 2;

    /*
     * @see android.os.SELinux#SELINUX_ANDROID_RESTORECON_RECURSE
     */
    public static final int SELINUX_ANDROID_RESTORECON_RECURSE = 4;

    /*
     * @see android.os.SELinux#SELINUX_ANDROID_RESTORECON_FORCE
     */
    public static final int SELINUX_ANDROID_RESTORECON_FORCE = 8;

    /*
     * @see android.os.SELinux#SELINUX_ANDROID_RESTORECON_DATADATA
     */
    public static final int SELINUX_ANDROID_RESTORECON_DATADATA = 16;

    public static final long UTIME_NOW = (1L << 30) - 1L;

    public static final long UTIME_OMIT = (1L << 30) - 2L;
}
