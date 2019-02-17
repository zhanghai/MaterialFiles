/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.system.OsConstants;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InterruptedIOException;

import androidx.annotation.NonNull;
import java8.nio.file.FileSystemException;
import java8.nio.file.StandardCopyOption;
import me.zhanghai.android.files.provider.common.CopyOptions;
import me.zhanghai.android.files.provider.linux.syscall.Constants;
import me.zhanghai.android.files.provider.linux.syscall.StructStat;
import me.zhanghai.android.files.provider.linux.syscall.StructTimespec;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;
import me.zhanghai.android.files.util.MoreTextUtils;

class LinuxCopyMove {

    private static final int SEND_FILE_COUNT = 8192;

    private LinuxCopyMove() {}

    public static void copy(@NonNull String source, @NonNull String target,
                            @NonNull CopyOptions copyOptions) throws IOException {
        if (copyOptions.hasAtomicMove()) {
            throw new UnsupportedOperationException(StandardCopyOption.ATOMIC_MOVE.toString());
        }
        copyInternal(source, target, copyOptions);
    }

    public static void move(@NonNull String source, @NonNull String target,
                            @NonNull CopyOptions copyOptions) throws IOException {
        try {
            Syscalls.rename(source, target);
            return;
        } catch (SyscallException e) {
            if (copyOptions.hasAtomicMove()) {
                e.maybeThrowAtomicMoveNotSupportedException(source, target);
                e.maybeThrowInvalidFileNameException(source, target);
                throw e.toFileSystemException(source, target);
            }
            // Ignored.
        }
        if (!copyOptions.hasCopyAttributes()) {
            copyOptions = new CopyOptions(copyOptions.hasReplaceExisting(), true,
                    copyOptions.hasAtomicMove(), copyOptions.hasNoFollowLinks(),
                    copyOptions.getProgressListener(), copyOptions.getProgressIntervalMillis());
        }
        copyInternal(source, target, copyOptions);
        try {
            Syscalls.remove(source);
        } catch (SyscallException e) {
            try {
                Syscalls.remove(target);
            } catch (SyscallException e2) {
                e.addSuppressed(e2.toFileSystemException(target));
            }
            throw e.toFileSystemException(source);
        }
    }

    private static void copyInternal(@NonNull String source, @NonNull String target,
                                     @NonNull CopyOptions copyOptions) throws IOException {
        StructStat sourceStat;
        try {
            sourceStat = copyOptions.hasNoFollowLinks() ? Syscalls.lstat(source) : Syscalls.stat(
                    source);
        } catch (SyscallException e) {
            throw e.toFileSystemException(source);
        }
        if (OsConstants.S_ISREG(sourceStat.st_mode)) {
            FileDescriptor sourceFd;
            try {
                sourceFd = Syscalls.open(source, OsConstants.O_RDONLY, 0);
            } catch (SyscallException e) {
                throw e.toFileSystemException(source);
            }
            try {
                int targetFlags = OsConstants.O_WRONLY | OsConstants.O_TRUNC
                        | OsConstants.O_CREAT;
                if (!copyOptions.hasReplaceExisting()) {
                    targetFlags |= OsConstants.O_EXCL;
                }
                FileDescriptor targetFd;
                try {
                    targetFd = Syscalls.open(target, targetFlags,
                            sourceStat.st_mode);
                } catch (SyscallException e) {
                    e.maybeThrowInvalidFileNameException(source, target);
                    throw e.toFileSystemException(target);
                }
                try {
                    long progressIntervalMillis = copyOptions.getProgressIntervalMillis();
                    long lastProgressMillis = System.currentTimeMillis();
                    long copiedByteCount = 0;
                    long sentByteCount;
                    while (true) {
                        try {
                            sentByteCount = Syscalls.sendfile(targetFd, sourceFd, null,
                                    SEND_FILE_COUNT);
                        } catch (SyscallException e) {
                            throw e.toFileSystemException(source, target);
                        }
                        if (sentByteCount == 0) {
                            break;
                        }
                        copiedByteCount += sentByteCount;
                        throwIfInterrupted();
                        long currentTimeMillis = System.currentTimeMillis();
                        if (copyOptions.hasProgressListener() && lastProgressMillis
                                + progressIntervalMillis < currentTimeMillis) {
                            copyOptions.getProgressListener().accept(copiedByteCount);
                            progressIntervalMillis = currentTimeMillis;
                        }
                    }
                    if (copyOptions.hasProgressListener()) {
                        copyOptions.getProgressListener().accept(copiedByteCount);
                    }
                } finally {
                    try {
                        Syscalls.close(targetFd);
                    } catch (SyscallException e) {
                        throw e.toFileSystemException(target);
                    }
                }
            } finally {
                try {
                    Syscalls.close(sourceFd);
                } catch (SyscallException e) {
                    throw e.toFileSystemException(source);
                }
            }
        } else if (OsConstants.S_ISDIR(sourceStat.st_mode)) {
            try {
                Syscalls.mkdir(target, sourceStat.st_mode);
            } catch (SyscallException e) {
                if (copyOptions.hasReplaceExisting() && e.getErrno() == OsConstants.EEXIST) {
                    try {
                        StructStat toStat = Syscalls.lstat(target);
                        if (!OsConstants.S_ISDIR(toStat.st_mode)) {
                            Syscalls.remove(target);
                            Syscalls.mkdir(target, sourceStat.st_mode);
                        }
                    } catch (SyscallException e2) {
                        e2.addSuppressed(e.toFileSystemException(target));
                        throw e2.toFileSystemException(target);
                    }
                }
                e.maybeThrowInvalidFileNameException(source, target);
                throw e.toFileSystemException(target);
            }
            if (copyOptions.hasProgressListener()) {
                copyOptions.getProgressListener().accept(sourceStat.st_size);
            }
        } else if (OsConstants.S_ISLNK(sourceStat.st_mode)) {
            String sourceTarget;
            try {
                sourceTarget = Syscalls.readlink(source);
            } catch (SyscallException e) {
                throw e.toFileSystemException(source);
            }
            try {
                Syscalls.symlink(sourceTarget, target);
            } catch (SyscallException e) {
                if (copyOptions.hasReplaceExisting() && e.getErrno() == OsConstants.EEXIST) {
                    try {
                        Syscalls.remove(target);
                        Syscalls.symlink(sourceTarget, target);
                    } catch (SyscallException e2) {
                        e2.addSuppressed(e.toFileSystemException(target));
                        throw e2.toFileSystemException(target);
                    }
                }
                e.maybeThrowInvalidFileNameException(source, target);
                throw e.toFileSystemException(target);
            }
            if (copyOptions.hasProgressListener()) {
                copyOptions.getProgressListener().accept(sourceStat.st_size);
            }
        } else {
            throw new FileSystemException(source, null, "st_mode " + sourceStat.st_mode);
        }
        // We don't take error when copying attribute fatal, so errors will only be logged from now
        // on.
        // Ownership should be copied before permissions so that special permission bits like
        // setuid work properly.
        try {
            if (copyOptions.hasCopyAttributes()) {
                Syscalls.lchown(target, sourceStat.st_uid, sourceStat.st_gid);
            }
        } catch (SyscallException e) {
            e.printStackTrace();
        }
        try {
            if (!OsConstants.S_ISLNK(sourceStat.st_mode)) {
                Syscalls.chmod(target, sourceStat.st_mode);
            }
        } catch (SyscallException e) {
            e.printStackTrace();
        }
        try {
            StructTimespec[] times = {
                    copyOptions.hasCopyAttributes() ? sourceStat.st_atim : new StructTimespec(0,
                            Constants.UTIME_OMIT),
                    sourceStat.st_mtim
            };
            Syscalls.lutimens(target, times);
        } catch (SyscallException e) {
            e.printStackTrace();
        }
        try {
            // TODO: Allow u+rw temporarily if we are to copy xattrs.
            String[] xattrNames = Syscalls.llistxattr(source);
            for (String xattrName : xattrNames) {
                if (!(copyOptions.hasCopyAttributes() || MoreTextUtils.startsWith(xattrName,
                        "user."))) {
                    continue;
                }
                byte[] xattrValue = Syscalls.lgetxattr(target, xattrName);
                Syscalls.lsetxattr(target, xattrName, xattrValue, 0);
            }
        } catch (SyscallException e) {
            e.printStackTrace();
        }
    }

    private static void throwIfInterrupted() throws InterruptedIOException {
        if (Thread.interrupted()) {
            throw new InterruptedIOException();
        }
    }
}
