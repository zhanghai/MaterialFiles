/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

import android.os.Build;
import android.system.ErrnoException;
import android.system.Int64Ref;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructPollfd;
import android.system.StructStatVfs;

import java.io.FileDescriptor;
import java.io.InterruptedIOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import me.zhanghai.android.files.compat.SELinuxCompat;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.libselinux.SeLinux;

public class Syscalls {

    private static final String LIBRARY_NAME = "syscalls";

    static {
        if (Os.getuid() != 0) {
            System.loadLibrary(LIBRARY_NAME);
        }
    }

    private Syscalls() {}

    @NonNull
    public static String getLibraryName() {
        return LIBRARY_NAME;
    }

    public static native boolean access(@NonNull ByteString path, int mode) throws SyscallException;

    public static native void chmod(@NonNull ByteString path, int mode) throws SyscallException;

    public static native void chown(@NonNull ByteString path, int uid, int gid)
            throws SyscallException;

    public static void close(@NonNull FileDescriptor fd) throws SyscallException {
        try {
            Os.close(fd);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    public static native void closedir(long dir) throws SyscallException;

    public static native void endgrent() throws SyscallException;

    public static native void endmntent(long file) throws SyscallException;

    public static native void endpwent() throws SyscallException;

    private static native int errno();

    public static int fcntl(@NonNull FileDescriptor fd, int cmd) throws SyscallException {
        return fcntl_void(fd, cmd);
    }

    public static int fcntl(@NonNull FileDescriptor fd, int cmd, int arg)
            throws SyscallException {
        return fcntl_int(fd, cmd, arg);
    }

    private static native int fcntl_int(@NonNull FileDescriptor fd, int cmd, int arg)
            throws SyscallException;

    private static native int fcntl_void(@NonNull FileDescriptor fd, int cmd)
            throws SyscallException;

    @NonNull
    public static ByteString getfilecon(@NonNull ByteString path) throws SyscallException {
        try {
            return ByteString.ofOwnableBytes(SeLinux.getfilecon(path.getOwnedBytes()));
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    @Nullable
    public static native StructGroup getgrent() throws SyscallException;

    @Nullable
    public static native StructGroup getgrgid(int gid) throws SyscallException;

    @Nullable
    public static native StructGroup getgrnam(@NonNull ByteString name) throws SyscallException;

    @Nullable
    public static native StructMntent getmntent(long file) throws SyscallException;

    @Nullable
    public static native StructPasswd getpwent() throws SyscallException;

    @Nullable
    public static native StructPasswd getpwnam(@NonNull ByteString name) throws SyscallException;

    @Nullable
    public static native StructPasswd getpwuid(int uid) throws SyscallException;

    public static native boolean hasmntopt(@NonNull StructMntent mntent,
                                           @NonNull ByteString option);

    public static native int inotify_add_watch(@NonNull FileDescriptor fd, @NonNull ByteString path,
                                               int mask) throws SyscallException;

    @NonNull
    public static native FileDescriptor inotify_init1(int flags) throws SyscallException;

    @NonNull
    public static native StructInotifyEvent[] inotify_get_events(@NonNull byte[] buffer, int offset,
                                                                 int length)
            throws SyscallException;

    public static native void inotify_rm_watch(@NonNull FileDescriptor fd, int wd)
            throws SyscallException;

    public static native int ioctl_int(@NonNull FileDescriptor fd, int request,
                                       @Nullable Int32Ref argument) throws SyscallException;

    public static boolean is_selinux_enabled() {
        return SeLinux.is_selinux_enabled();
    }

    public static native void lchown(@NonNull ByteString path, int uid, int gid)
            throws SyscallException;

    @NonNull
    public static ByteString lgetfilecon(@NonNull ByteString path) throws SyscallException {
        try {
            return ByteString.ofOwnableBytes(SeLinux.lgetfilecon(path.getOwnedBytes()));
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    public static void lsetfilecon(@NonNull ByteString path, @NonNull ByteString context)
            throws SyscallException {
        try {
            SeLinux.lsetfilecon(path.getOwnedBytes(), context.getOwnedBytes());
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    @NonNull
    public static native byte[] lgetxattr(@NonNull ByteString path, @NonNull ByteString name)
            throws SyscallException;

    public static native void link(@NonNull ByteString oldPath, @NonNull ByteString newPath)
            throws SyscallException;

    @NonNull
    public static native ByteString[] llistxattr(@NonNull ByteString path) throws SyscallException;

    public static native void lsetxattr(@NonNull ByteString path, @NonNull ByteString name,
                                        @NonNull byte[] value, int flags) throws SyscallException;

    @NonNull
    public static native StructStat lstat(@NonNull ByteString path) throws SyscallException;

    public static native void lutimens(@NonNull ByteString path,
                                       @NonNull @Size(2) StructTimespec[] times)
            throws SyscallException;

    public static native void mkdir(@NonNull ByteString path, int mode) throws SyscallException;

    public static native int mount(@Nullable ByteString source, @NonNull ByteString target,
                                   @Nullable ByteString fileSystemType, long mountFlags,
                                   @Nullable byte[] data) throws SyscallException;

    @NonNull
    public static native FileDescriptor open(@NonNull ByteString path, int flags, int mode)
            throws SyscallException;

    public static native long opendir(@NonNull ByteString path) throws SyscallException;

    public static int poll(@NonNull StructPollfd[] fds, int timeout) throws SyscallException {
        try {
            return Os_poll(fds, timeout);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    private static int Os_poll(@NonNull StructPollfd[] fds, int timeout) throws ErrnoException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || timeout < 0) {
            return Os.poll(fds, timeout);
        } else {
            long timeoutTime = System.currentTimeMillis() + timeout;
            while (true) {
                try {
                    return Os.poll(fds, timeout);
                } catch (ErrnoException e) {
                    if (e.errno == OsConstants.EINTR) {
                        long newTimeout = timeoutTime - System.currentTimeMillis();
                        if (newTimeout <= 0) {
                            return 0;
                        }
                        timeout = (int) newTimeout;
                        continue;
                    }
                    throw e;
                }
            }
        }
    }

    public static int read(@NonNull FileDescriptor fd, @NonNull byte[] buffer)
            throws InterruptedIOException, SyscallException {
        return read(fd, buffer, 0, buffer.length);
    }

    public static int read(@NonNull FileDescriptor fd, @NonNull byte[] buffer, int offset,
                           int length) throws InterruptedIOException, SyscallException {
        try {
            return Os.read(fd, buffer, offset, length);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    @Nullable
    public static native StructDirent readdir(long dir) throws SyscallException;

    @NonNull
    public static native ByteString readlink(@NonNull ByteString path) throws SyscallException;

    @NonNull
    public static native ByteString realpath(@NonNull ByteString path) throws SyscallException;

    public static native void remove(@NonNull ByteString path) throws SyscallException;

    public static native void rename(@NonNull ByteString oldPath, @NonNull ByteString newPath)
            throws SyscallException;

    public static boolean security_getenforce() throws SyscallException {
        try {
            return SeLinux.security_getenforce();
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    public static void selinux_android_restorecon(@NonNull ByteString path, int flags)
            throws SyscallException {
        // FIXME: Platform SELinux class cannot accept byte array, so we have to use a string.
        boolean successful = SELinuxCompat.native_restorecon(path.toString(), flags);
        if (!successful) {
            int errno = errno();
            if (errno == 0) {
                // Just set some generic error.
                errno = OsConstants.EIO;
            }
            throw new SyscallException("selinux_android_restorecon", errno);
        }
    }

    public static native long sendfile(@NonNull FileDescriptor outFd, @NonNull FileDescriptor inFd,
                                       @Nullable Int64Ref offset, long count)
            throws SyscallException;

    public static void setfilecon(@NonNull ByteString path, @NonNull ByteString context)
            throws SyscallException {
        try {
            SeLinux.setfilecon(path.getOwnedBytes(), context.getOwnedBytes());
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    public static native void setgrent() throws SyscallException;

    public static native long setmntent(@NonNull ByteString path, @NonNull ByteString mode)
            throws SyscallException;

    public static native void setpwent() throws SyscallException;

    @NonNull
    @Size(2)
    public static FileDescriptor[] socketpair(int domain, int type, int protocol)
            throws SyscallException {
        FileDescriptor[] fds = new FileDescriptor[] { new FileDescriptor(), new FileDescriptor() };
        try {
            Os.socketpair(domain, type, protocol, fds[0], fds[1]);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
        return fds;
    }

    @NonNull
    public static native StructStat stat(@NonNull ByteString path) throws SyscallException;

    @NonNull
    public static native StructStatVfs statvfs(@NonNull ByteString path) throws SyscallException;

    @NonNull
    public static String strerror(int errno) {
        return Os.strerror(errno);
    }

    public static native void symlink(@NonNull ByteString target, @NonNull ByteString linkPath)
            throws SyscallException;

    public static native void utimens(@NonNull ByteString path,
                                      @NonNull @Size(2) StructTimespec[] times)
            throws SyscallException;

    public static int write(@NonNull FileDescriptor fd, @NonNull byte[] buffer)
            throws InterruptedIOException, SyscallException {
        return write(fd, buffer, 0, buffer.length);
    }

    public static int write(@NonNull FileDescriptor fd, @NonNull byte[] buffer, int offset,
                            int length) throws InterruptedIOException, SyscallException {
        try {
            return Os.write(fd, buffer, offset, length);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }
}
