/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

import android.system.ErrnoException;
import android.system.Int64Ref;
import android.system.Os;

import java.io.FileDescriptor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

public class Syscalls {

    static {
        System.loadLibrary("syscalls");
    }

    public static void chmod(@NonNull String path, int mode) throws SyscallException {
        try {
            Os.chmod(path, mode);
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

    @Nullable
    public static native StructGroup getgrgid(int gid) throws SyscallException;

    @Nullable
    public static native StructGroup getgrnam(@NonNull String name) throws SyscallException;

    @Nullable
    public static native StructPasswd getpwnam(@NonNull String name) throws SyscallException;

    @Nullable
    public static native StructPasswd getpwuid(int uid) throws SyscallException;

    public static void lchown(@NonNull String path, int uid, int gid) throws SyscallException {
        try {
            Os.lchown(path, uid, gid);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    @NonNull
    public static native byte[] lgetxattr(@NonNull String path, @NonNull String name)
            throws SyscallException;

    @NonNull
    public static native String[] listdir(@NonNull String path) throws SyscallException;

    @NonNull
    public static native String[] llistxattr(@NonNull String path) throws SyscallException;

    public static native void lsetxattr(@NonNull String path, @NonNull String name,
                                        @NonNull byte[] value, int flags) throws SyscallException;

    @NonNull
    public static native StructStatCompat lstat(@NonNull String path) throws SyscallException;

    //public static native void lutimens(@NonNull String path,
    //                                   @NonNull @Size(2) StructTimespec[] times)
    public static native void lutimens(@NonNull String path,
                                       @NonNull @Size(2) StructTimespecCompat[] times)
            throws SyscallException;

    public static void mkdir(@NonNull String path, int mode) throws SyscallException {
        try {
            Os.mkdir(path, mode);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    @NonNull
    public static FileDescriptor open(@NonNull String path, int flags, int mode)
            throws SyscallException {
        try {
            return Os.open(path, flags, mode);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    @NonNull
    public static String readlink(@NonNull String path) throws SyscallException {
        try {
            return Os.readlink(path);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    public static void remove(@NonNull String path) throws SyscallException {
        try {
            Os.remove(path);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    public static void rename(@NonNull String oldPath, @NonNull String newPath)
            throws SyscallException {
        try {
            Os.rename(oldPath, newPath);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }

    public static native long sendfile(@NonNull FileDescriptor outFd, @NonNull FileDescriptor inFd,
                                       @Nullable Int64Ref offset, long count)
            throws SyscallException;

    @NonNull
    public static native StructStatCompat stat(@NonNull String path) throws SyscallException;

    @NonNull
    public static String strerror(int errno) {
        return Os.strerror(errno);
    }

    public static void symlink(@NonNull String oldPath, @NonNull String newPath)
            throws SyscallException {
        try {
            Os.symlink(oldPath, newPath);
        } catch (ErrnoException e) {
            throw new SyscallException(e);
        }
    }
}
