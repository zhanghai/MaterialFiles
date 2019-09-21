/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

import android.system.ErrnoException;
import android.system.OsConstants;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.AccessDeniedException;
import java8.nio.file.AtomicMoveNotSupportedException;
import java8.nio.file.DirectoryNotEmptyException;
import java8.nio.file.FileAlreadyExistsException;
import java8.nio.file.FileSystemException;
import java8.nio.file.FileSystemLoopException;
import java8.nio.file.NoSuchFileException;
import java8.nio.file.NotDirectoryException;
import me.zhanghai.android.files.compat.ErrnoExceptionCompat;
import me.zhanghai.android.files.provider.common.InvalidFileNameException;
import me.zhanghai.android.files.provider.common.IsDirectoryException;
import me.zhanghai.android.files.provider.common.ReadOnlyFileSystemException;

public class SyscallException extends Exception {

    @NonNull
    private final String mFunctionName;

    private final int mErrno;

    public SyscallException(@NonNull String functionName, int errno, @Nullable Throwable cause) {
        super(perror(errno, functionName), cause);

        mFunctionName = functionName;
        mErrno = errno;
    }

    public SyscallException(@NonNull String functionName, int errno) {
        this(functionName, errno, null);
    }

    public SyscallException(@NonNull ErrnoException errnoException) {
        this(ErrnoExceptionCompat.getFunctionName(errnoException), errnoException.errno,
                errnoException);
    }

    @NonNull
    public String getFunctionName() {
        return mFunctionName;
    }

    public int getErrno() {
        return mErrno;
    }

    @NonNull
    private static String perror(int errno, @NonNull String functionName) {
        return functionName + ": " + Syscalls.strerror(errno);
    }

    public void maybeThrowAtomicMoveNotSupportedException(@Nullable String file,
                                                          @Nullable String other)
            throws AtomicMoveNotSupportedException {
        if (mErrno == OsConstants.EXDEV) {
            AtomicMoveNotSupportedException exception = new AtomicMoveNotSupportedException(file,
                    other, getMessage());
            exception.initCause(this);
            throw exception;
        }
    }

    public void maybeThrowInvalidFileNameException(@Nullable String file)
            throws InvalidFileNameException {
        if (mErrno == OsConstants.EINVAL) {
            InvalidFileNameException exception = new InvalidFileNameException(file, null,
                    getMessage());
            exception.initCause(this);
            throw exception;
        }
    }

    @NonNull
    public FileSystemException toFileSystemException(@Nullable String file,
                                                     @Nullable String other) {
        FileSystemException fileSystemException = toFileSystemExceptionWithoutCause(file, other);
        fileSystemException.initCause(this);
        return fileSystemException;
    }

    @NonNull
    public FileSystemException toFileSystemException(@Nullable String file) {
        return toFileSystemException(file, null);
    }

    @NonNull
    private FileSystemException toFileSystemExceptionWithoutCause(@Nullable String file,
                                                                  @Nullable String other) {
        if (mErrno == OsConstants.EACCES || mErrno == OsConstants.EPERM) {
            return new AccessDeniedException(file, other, getMessage());
        } else if (mErrno == OsConstants.EEXIST) {
            return new FileAlreadyExistsException(file, other, getMessage());
        } else if (mErrno == OsConstants.EISDIR) {
            return new IsDirectoryException(file, other, getMessage());
        } else if (mErrno == OsConstants.ELOOP) {
            return new FileSystemLoopException(file);
        } else if (mErrno == OsConstants.ENOTDIR) {
            return new NotDirectoryException(file);
        } else if (mErrno == OsConstants.ENOTEMPTY) {
            return new DirectoryNotEmptyException(file);
        } else if (mErrno == OsConstants.ENOENT) {
            return new NoSuchFileException(file, other, getMessage());
        } else if (mErrno == OsConstants.EROFS) {
            return new ReadOnlyFileSystemException(file, other, getMessage());
        } else {
            return new FileSystemException(file, other, getMessage());
        }
    }
}
