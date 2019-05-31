/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

import android.os.Build;
import android.system.OsConstants;

public class Constants {

    private Constants() {}

    public static final int IN_ACCESS = 0x00000001;

    public static final int IN_MODIFY = 0x00000002;

    public static final int IN_ATTRIB = 0x00000004;

    public static final int IN_CLOSE_WRITE = 0x00000008;

    public static final int IN_CLOSE_NOWRITE = 0x00000010;

    public static final int IN_OPEN = 0x00000020;

    public static final int IN_MOVED_FROM = 0x00000040;

    public static final int IN_MOVED_TO = 0x00000080;

    public static final int IN_CREATE = 0x00000100;

    public static final int IN_DELETE = 0x00000200;

    public static final int IN_DELETE_SELF = 0x00000400;

    public static final int IN_MOVE_SELF = 0x00000800;

    public static final int IN_UNMOUNT = 0x00002000;

    public static final int IN_Q_OVERFLOW = 0x00004000;

    public static final int IN_IGNORED = 0x00008000;

    public static final int IN_CLOSE = (IN_CLOSE_WRITE | IN_CLOSE_NOWRITE);

    public static final int IN_MOVE = (IN_MOVED_FROM | IN_MOVED_TO);

    public static final int IN_ONLYDIR = 0x01000000;

    public static final int IN_DONT_FOLLOW = 0x02000000;

    public static final int IN_EXCL_UNLINK = 0x04000000;

    public static final int IN_MASK_ADD = 0x20000000;

    public static final int IN_ISDIR = 0x40000000;

    public static final int IN_ONESHOT = 0x80000000;

    public static final int IN_ALL_EVENTS = (IN_ACCESS | IN_MODIFY | IN_ATTRIB | IN_CLOSE_WRITE
            | IN_CLOSE_NOWRITE | IN_OPEN | IN_MOVED_FROM | IN_MOVED_TO | IN_DELETE | IN_CREATE
            | IN_DELETE_SELF | IN_MOVE_SELF);

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
