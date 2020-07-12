/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall

import android.os.Build
import android.system.OsConstants

object Constants {
    // 0x0000125D
    const val BLKROSET = (0x12 shl 8) or 93

    const val IN_ACCESS = 0x00000001
    const val IN_MODIFY = 0x00000002
    const val IN_ATTRIB = 0x00000004
    const val IN_CLOSE_WRITE = 0x00000008
    const val IN_CLOSE_NOWRITE = 0x00000010
    const val IN_OPEN = 0x00000020
    const val IN_MOVED_FROM = 0x00000040
    const val IN_MOVED_TO = 0x00000080
    const val IN_CREATE = 0x00000100
    const val IN_DELETE = 0x00000200
    const val IN_DELETE_SELF = 0x00000400
    const val IN_MOVE_SELF = 0x00000800
    const val IN_UNMOUNT = 0x00002000
    const val IN_Q_OVERFLOW = 0x00004000
    const val IN_IGNORED = 0x00008000
    const val IN_CLOSE = IN_CLOSE_WRITE or IN_CLOSE_NOWRITE
    const val IN_MOVE = IN_MOVED_FROM or IN_MOVED_TO
    const val IN_ONLYDIR = 0x01000000
    const val IN_DONT_FOLLOW = 0x02000000
    const val IN_EXCL_UNLINK = 0x04000000
    const val IN_MASK_ADD = 0x20000000
    const val IN_ISDIR = 0x40000000
    const val IN_ONESHOT = 0x80000000.toInt()
    const val IN_ALL_EVENTS = (IN_ACCESS or IN_MODIFY or IN_ATTRIB or IN_CLOSE_WRITE
        or IN_CLOSE_NOWRITE or IN_OPEN or IN_MOVED_FROM or IN_MOVED_TO or IN_DELETE or IN_CREATE
        or IN_DELETE_SELF or IN_MOVE_SELF)

    const val MS_RDONLY = 1L
    const val MS_NOSUID = 2L
    const val MS_NODEV = 4L
    const val MS_NOEXEC = 8L
    const val MS_SYNCHRONOUS = 16L
    const val MS_REMOUNT = 32L
    const val MS_MANDLOCK = 64L
    const val MS_DIRSYNC = 128L
    const val MS_NOATIME = 1024L
    const val MS_NODIRATIME = 2048L
    const val MS_BIND = 4096L
    const val MS_MOVE = 8192L
    const val MS_REC = 16384L
    const val MS_VERBOSE = 32768L
    const val MS_SILENT = 32768L
    const val MS_POSIXACL = 1L shl 16
    const val MS_UNBINDABLE = 1L shl 17
    const val MS_PRIVATE = 1L shl 18
    const val MS_SLAVE = 1L shl 19
    const val MS_SHARED = 1L shl 20
    const val MS_RELATIME = 1L shl 21
    const val MS_KERNMOUNT = 1L shl 22
    const val MS_I_VERSION = 1L shl 23
    const val MS_STRICTATIME = 1L shl 24
    const val MS_LAZYTIME = 1L shl 25
    const val MS_SUBMOUNT = 1L shl 26
    const val MS_NOREMOTELOCK = 1L shl 27
    const val MS_NOSEC = 1L shl 28
    const val MS_BORN = 1L shl 29
    const val MS_ACTIVE = 1L shl 30
    const val MS_NOUSER = 1L shl 31
    const val MS_RMT_MASK = (MS_RDONLY or MS_SYNCHRONOUS or MS_MANDLOCK or MS_I_VERSION
        or MS_LAZYTIME)
    const val MS_MGC_VAL = 0xC0ED0000L
    const val MS_MGC_MSK = 0xffff0000L

    val O_DSYNC =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) OsConstants.O_DSYNC else 0x1000

    /* @see android.os.SELinux#SELINUX_ANDROID_RESTORECON_NOCHANGE */
    const val SELINUX_ANDROID_RESTORECON_NOCHANGE = 1
    /* @see android.os.SELinux#SELINUX_ANDROID_RESTORECON_VERBOSE */
    const val SELINUX_ANDROID_RESTORECON_VERBOSE = 2
    /* @see android.os.SELinux#SELINUX_ANDROID_RESTORECON_RECURSE */
    const val SELINUX_ANDROID_RESTORECON_RECURSE = 4
    /* @see android.os.SELinux#SELINUX_ANDROID_RESTORECON_FORCE */
    const val SELINUX_ANDROID_RESTORECON_FORCE = 8
    /* @see android.os.SELinux#SELINUX_ANDROID_RESTORECON_DATADATA */
    const val SELINUX_ANDROID_RESTORECON_DATADATA = 16

    const val UTIME_NOW = (1L shl 30) - 1L
    const val UTIME_OMIT = (1L shl 30) - 2L
}
