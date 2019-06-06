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
import java8.nio.file.FileAlreadyExistsException;
import java8.nio.file.FileSystemException;
import java8.nio.file.StandardCopyOption;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.CopyOptions;
import me.zhanghai.android.files.provider.linux.syscall.Constants;
import me.zhanghai.android.files.provider.linux.syscall.StructStat;
import me.zhanghai.android.files.provider.linux.syscall.StructTimespec;
import me.zhanghai.android.files.provider.linux.syscall.SyscallException;
import me.zhanghai.android.files.provider.linux.syscall.Syscalls;

class LinuxCopyMove {

    private static final int SEND_FILE_COUNT = 8192;

    private static final ByteString XATTR_NAME_PREFIX_USER = ByteString.fromString("user.");

    private LinuxCopyMove() {}

    public static void copy(@NonNull ByteString source, @NonNull ByteString target,
                            @NonNull CopyOptions copyOptions) throws IOException {
        if (copyOptions.hasAtomicMove()) {
            throw new UnsupportedOperationException(StandardCopyOption.ATOMIC_MOVE.toString());
        }
        StructStat sourceStat;
        try {
            sourceStat = copyOptions.hasNoFollowLinks() ? Syscalls.lstat(source) : Syscalls.stat(
                    source);
        } catch (SyscallException e) {
            throw e.toFileSystemException(source.toString());
        }
        StructStat targetStat;
        try {
            targetStat = Syscalls.lstat(target);
        } catch (SyscallException e) {
            if (e.getErrno() != OsConstants.ENOENT) {
                throw e.toFileSystemException(target.toString());
            }
            // Ignored.
            targetStat = null;
        }
        if (targetStat != null) {
            if (sourceStat.st_dev == targetStat.st_dev && sourceStat.st_ino == targetStat.st_ino) {
                if (copyOptions.hasProgressListener()) {
                    copyOptions.getProgressListener().accept(sourceStat.st_size);
                }
                return;
            }
            if (!copyOptions.hasReplaceExisting()) {
                throw new FileAlreadyExistsException(source.toString(), target.toString(), null);
            }
            try {
                Syscalls.remove(target);
            } catch (SyscallException e) {
                throw e.toFileSystemException(target.toString());
            }
        }
        if (OsConstants.S_ISREG(sourceStat.st_mode)) {
            FileDescriptor sourceFd;
            try {
                sourceFd = Syscalls.open(source, OsConstants.O_RDONLY, 0);
            } catch (SyscallException e) {
                throw e.toFileSystemException(source.toString());
            }
            try {
                int targetFlags = OsConstants.O_WRONLY | OsConstants.O_TRUNC
                        | OsConstants.O_CREAT;
                if (!copyOptions.hasReplaceExisting()) {
                    targetFlags |= OsConstants.O_EXCL;
                }
                FileDescriptor targetFd;
                try {
                    targetFd = Syscalls.open(target, targetFlags, sourceStat.st_mode);
                } catch (SyscallException e) {
                    e.maybeThrowInvalidFileNameException(target.toString());
                    throw e.toFileSystemException(target.toString());
                }
                boolean successful = false;
                try {
                    long progressIntervalMillis = copyOptions.getProgressIntervalMillis();
                    long lastProgressMillis = System.currentTimeMillis();
                    long copiedSize = 0;
                    while (true) {
                        long sentSize;
                        try {
                            sentSize = Syscalls.sendfile(targetFd, sourceFd, null, SEND_FILE_COUNT);
                        } catch (SyscallException e) {
                            throw e.toFileSystemException(source.toString(), target.toString());
                        }
                        if (sentSize == 0) {
                            break;
                        }
                        copiedSize += sentSize;
                        throwIfInterrupted();
                        long currentTimeMillis = System.currentTimeMillis();
                        if (copyOptions.hasProgressListener() && currentTimeMillis >=
                                lastProgressMillis + progressIntervalMillis) {
                            copyOptions.getProgressListener().accept(copiedSize);
                            lastProgressMillis = currentTimeMillis;
                            copiedSize = 0;
                        }
                    }
                    if (copyOptions.hasProgressListener()) {
                        copyOptions.getProgressListener().accept(copiedSize);
                    }
                    successful = true;
                } finally {
                    try {
                        Syscalls.close(targetFd);
                    } catch (SyscallException e) {
                        throw e.toFileSystemException(target.toString());
                    } finally {
                        if (!successful) {
                            try {
                                Syscalls.remove(target);
                            } catch (SyscallException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } finally {
                try {
                    Syscalls.close(sourceFd);
                } catch (SyscallException e) {
                    throw e.toFileSystemException(source.toString());
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
                        e2.addSuppressed(e.toFileSystemException(target.toString()));
                        throw e2.toFileSystemException(target.toString());
                    }
                }
                e.maybeThrowInvalidFileNameException(target.toString());
                throw e.toFileSystemException(target.toString());
            }
            if (copyOptions.hasProgressListener()) {
                copyOptions.getProgressListener().accept(sourceStat.st_size);
            }
        } else if (OsConstants.S_ISLNK(sourceStat.st_mode)) {
            ByteString sourceTarget;
            try {
                sourceTarget = Syscalls.readlink(source);
            } catch (SyscallException e) {
                throw e.toFileSystemException(source.toString());
            }
            try {
                Syscalls.symlink(sourceTarget, target);
            } catch (SyscallException e) {
                if (copyOptions.hasReplaceExisting() && e.getErrno() == OsConstants.EEXIST) {
                    try {
                        Syscalls.remove(target);
                        Syscalls.symlink(sourceTarget, target);
                    } catch (SyscallException e2) {
                        e2.addSuppressed(e.toFileSystemException(target.toString()));
                        throw e2.toFileSystemException(target.toString());
                    }
                }
                e.maybeThrowInvalidFileNameException(target.toString());
                throw e.toFileSystemException(target.toString());
            }
            if (copyOptions.hasProgressListener()) {
                copyOptions.getProgressListener().accept(sourceStat.st_size);
            }
        } else {
            throw new FileSystemException(source.toString(), null, "st_mode " + sourceStat.st_mode);
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
        // TODO: Change modified time last?
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
            ByteString[] xattrNames = Syscalls.llistxattr(source);
            for (ByteString xattrName : xattrNames) {
                if (!(copyOptions.hasCopyAttributes() || xattrName.startsWith(
                        XATTR_NAME_PREFIX_USER))) {
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

    public static void move(@NonNull ByteString source, @NonNull ByteString target,
                            @NonNull CopyOptions copyOptions) throws IOException {
        StructStat sourceStat;
        try {
            sourceStat = Syscalls.lstat(source);
        } catch (SyscallException e) {
            throw e.toFileSystemException(source.toString());
        }
        StructStat targetStat;
        try {
            targetStat = Syscalls.lstat(target);
        } catch (SyscallException e) {
            if (e.getErrno() != OsConstants.ENOENT) {
                throw e.toFileSystemException(target.toString());
            }
            // Ignored.
            targetStat = null;
        }
        if (targetStat != null) {
            if (sourceStat.st_dev == targetStat.st_dev && sourceStat.st_ino == targetStat.st_ino) {
                if (copyOptions.hasProgressListener()) {
                    copyOptions.getProgressListener().accept(sourceStat.st_size);
                }
                return;
            }
            if (!copyOptions.hasReplaceExisting()) {
                throw new FileAlreadyExistsException(source.toString(), target.toString(), null);
            }
            try {
                Syscalls.remove(target);
            } catch (SyscallException e) {
                throw e.toFileSystemException(target.toString());
            }
        }
        boolean renameSuccessful = false;
        try {
            Syscalls.rename(source, target);
            renameSuccessful = true;
        } catch (SyscallException e) {
            if (copyOptions.hasAtomicMove()) {
                e.maybeThrowAtomicMoveNotSupportedException(source.toString(), target.toString());
                e.maybeThrowInvalidFileNameException(target.toString());
                throw e.toFileSystemException(source.toString(), target.toString());
            }
            // Ignored.
        }
        if (renameSuccessful) {
            if (copyOptions.hasProgressListener()) {
                copyOptions.getProgressListener().accept(sourceStat.st_size);
            }
            return;
        }
        if (copyOptions.hasAtomicMove()) {
            throw new AssertionError();
        }
        if (!copyOptions.hasCopyAttributes() || !copyOptions.hasNoFollowLinks()) {
            copyOptions = new CopyOptions(copyOptions.hasReplaceExisting(), true, false, true,
                    copyOptions.getProgressListener(), copyOptions.getProgressIntervalMillis());
        }
        copy(source, target, copyOptions);
        try {
            Syscalls.remove(source);
        } catch (SyscallException e) {
            try {
                Syscalls.remove(target);
            } catch (SyscallException e2) {
                e.addSuppressed(e2.toFileSystemException(target.toString()));
            }
            throw e.toFileSystemException(source.toString());
        }
    }
}
