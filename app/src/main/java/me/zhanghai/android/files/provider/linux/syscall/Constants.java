/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

import android.os.Build;
import android.system.OsConstants;

public class Constants {

    private Constants() {}

    // 0x0000125D
    public static final int BLKROSET = (0x12 << 8) | 93;

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

    public static final long MS_RDONLY = 1;

    public static final long MS_NOSUID = 2;

    public static final long MS_NODEV = 4;

    public static final long MS_NOEXEC = 8;

    public static final long MS_SYNCHRONOUS = 16;

    public static final long MS_REMOUNT = 32;

    public static final long MS_MANDLOCK = 64;

    public static final long MS_DIRSYNC = 128;

    public static final long MS_NOATIME = 1024;

    public static final long MS_NODIRATIME = 2048;

    public static final long MS_BIND = 4096;

    public static final long MS_MOVE = 8192;

    public static final long MS_REC = 16384;

    public static final long MS_VERBOSE = 32768;

    public static final long MS_SILENT = 32768;

    public static final long MS_POSIXACL = 1 << 16;

    public static final long MS_UNBINDABLE = 1 << 17;

    public static final long MS_PRIVATE = 1 << 18;

    public static final long MS_SLAVE = 1 << 19;

    public static final long MS_SHARED = 1 << 20;

    public static final long MS_RELATIME = 1 << 21;

    public static final long MS_KERNMOUNT = 1 << 22;

    public static final long MS_I_VERSION = 1 << 23;

    public static final long MS_STRICTATIME = 1 << 24;

    public static final long MS_LAZYTIME = 1 << 25;

    public static final long MS_SUBMOUNT = 1 << 26;

    public static final long MS_NOREMOTELOCK = 1 << 27;

    public static final long MS_NOSEC = 1 << 28;

    public static final long MS_BORN = 1 << 29;

    public static final long MS_ACTIVE = 1 << 30;

    public static final long MS_NOUSER = 1 << 31;

    public static final long MS_RMT_MASK = MS_RDONLY | MS_SYNCHRONOUS | MS_MANDLOCK | MS_I_VERSION
            | MS_LAZYTIME;

    public static final long MS_MGC_VAL = 0xC0ED0000;

    public static final long MS_MGC_MSK = 0xffff0000;

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
