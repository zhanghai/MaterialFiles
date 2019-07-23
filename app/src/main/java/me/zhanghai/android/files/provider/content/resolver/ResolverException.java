/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content.resolver;

import java.io.FileNotFoundException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.AccessDeniedException;
import java8.nio.file.FileSystemException;
import java8.nio.file.NoSuchFileException;

public class ResolverException extends Exception {

    public ResolverException(@Nullable String message) {
        super(message);
    }

    public ResolverException(@Nullable Throwable cause) {
        super(cause);
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
        Throwable cause = getCause();
        if (cause instanceof FileNotFoundException) {
            return new NoSuchFileException(file, other, getMessage());
        } else if (cause instanceof SecurityException) {
            return new AccessDeniedException(file, other, getMessage());
        } else {
            return new FileSystemException(file, other, getMessage());
        }
    }
}
