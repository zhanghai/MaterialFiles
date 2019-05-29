/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

import android.system.ErrnoException;
import android.system.Int64Ref;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStatVfs;

import java.io.FileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.reflected.ReflectedAccessor;
import me.zhanghai.android.files.util.SELinuxCompat;
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

    public static boolean access(@NonNull ByteString path, int mode) throws SyscallException {
        try {
            // TODO
            return Os.access(path.toString(), mode);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    public static void chmod(@NonNull ByteString path, int mode) throws SyscallException {
        try {
            // TODO
            Os.chmod(path.toString(), mode);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    public static void chown(@NonNull ByteString path, int uid, int gid) throws SyscallException {
        try {
            // TODO
            Os.chown(path.toString(), uid, gid);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    public static void close(@NonNull FileDescriptor fd) throws SyscallException {
        try {
            Os.close(fd);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    public static native void closedir(long dir) throws SyscallException;

    private static native int errno();

    @NonNull
    public static ByteString getfilecon(@NonNull ByteString path) throws SyscallException {
        try {
            // TODO
            return ByteString.fromString(SeLinux.getfilecon(path.toString()));
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    @Nullable
    public static native StructGroup getgrgid(int gid) throws SyscallException;

    @Nullable
    public static native StructGroup getgrnam(@NonNull ByteString name) throws SyscallException;

    @Nullable
    public static native StructPasswd getpwnam(@NonNull ByteString name) throws SyscallException;

    @Nullable
    public static native StructPasswd getpwuid(int uid) throws SyscallException;

    public static boolean is_selinux_enabled() {
        return SeLinux.is_selinux_enabled();
    }

    public static void lchown(@NonNull ByteString path, int uid, int gid) throws SyscallException {
        try {
            // TODO
            Os.lchown(path.toString(), uid, gid);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    @NonNull
    public static ByteString lgetfilecon(@NonNull ByteString path) throws SyscallException {
        try {
            // TODO
            return ByteString.fromString(SeLinux.lgetfilecon(path.toString()));
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    public static void lsetfilecon(@NonNull ByteString path, @NonNull ByteString context)
            throws SyscallException {
        try {
            // TODO
            SeLinux.lsetfilecon(path.toString(), context.toString());
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    @NonNull
    public static native byte[] lgetxattr(@NonNull ByteString path, @NonNull ByteString name)
            throws SyscallException;

    public static void link(@NonNull ByteString oldPath, @NonNull ByteString newPath)
            throws SyscallException {
        try {
            // TODO
            Os.link(oldPath.toString(), newPath.toString());
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    @NonNull
    public static native ByteString[] llistxattr(@NonNull ByteString path) throws SyscallException;

    public static native void lsetxattr(@NonNull ByteString path, @NonNull ByteString name,
                                        @NonNull byte[] value, int flags) throws SyscallException;

    @NonNull
    public static native StructStat lstat(@NonNull ByteString path) throws SyscallException;

    public static native void lutimens(@NonNull ByteString path,
                                       @NonNull @Size(2) StructTimespec[] times)
            throws SyscallException;

    public static void mkdir(@NonNull ByteString path, int mode) throws SyscallException {
        try {
            // TODO
            Os.mkdir(path.toString(), mode);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    @NonNull
    public static FileDescriptor open(@NonNull ByteString path, int flags, int mode)
            throws SyscallException {
        try {
            // TODO
            return Os.open(path.toString(), flags, mode);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    public static native long opendir(@NonNull ByteString path) throws SyscallException;

    @Nullable
    public static native StructDirent readdir(long dir) throws SyscallException;

    @NonNull
    public static ByteString readlink(@NonNull ByteString path) throws SyscallException {
        try {
            // TODO
            return ByteString.fromString(Os.readlink(path.toString()));
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    @NonNull
    public static native ByteString realpath(@NonNull ByteString path) throws SyscallException;

    public static void remove(@NonNull ByteString path) throws SyscallException {
        try {
            // TODO
            Os.remove(path.toString());
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    public static void rename(@NonNull ByteString oldPath, @NonNull ByteString newPath)
            throws SyscallException {
        try {
            // TODO
            Os.rename(oldPath.toString(), newPath.toString());
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    public static boolean security_getenforce() throws SyscallException {
        try {
            return SeLinux.security_getenforce();
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    static {
        ReflectedAccessor.allowRestrictedHiddenApiAccess();
    }

    public static void selinux_android_restorecon(@NonNull ByteString path, int flags)
            throws SyscallException {
        // TODO
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
            // TODO
            SeLinux.setfilecon(path.toString(), context.toString());
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    @NonNull
    public static native StructStat stat(@NonNull ByteString path) throws SyscallException;

    @NonNull
    public static StructStatVfs statvfs(@NonNull ByteString path) throws SyscallException {
        try {
            // TODO
            return Os.statvfs(path.toString());
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    @NonNull
    public static String strerror(int errno) {
        return Os.strerror(errno);
    }

    public static void symlink(@NonNull ByteString target, @NonNull ByteString linkPath)
            throws SyscallException {
        try {
            // TODO
            Os.symlink(target.toString(), linkPath.toString());
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    public static native void utimens(@NonNull ByteString path,
                                      @NonNull @Size(2) StructTimespec[] times)
            throws SyscallException;
}
