/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

import android.system.ErrnoException;
import android.system.OsConstants;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.FileSystemException;
import me.zhanghai.android.files.reflected.ReflectedAccessor;
import me.zhanghai.android.files.reflected.ReflectedField;
import me.zhanghai.android.files.reflected.RestrictedHiddenApi;

public class SyscallException extends FileSystemException {

    @NonNull
    private final String mFunctionName;

    private final int mErrno;

    public SyscallException(@NonNull String functionName, int errno,
                            @Nullable Throwable cause) {
        super(null, null, functionName);

        Objects.requireNonNull(functionName);
        mFunctionName = functionName;
        mErrno = errno;
        if (cause != null) {
            initCause(cause);
        }
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

    @Override
    public String getMessage() {
        String errnoName = OsConstants.errnoName(mErrno);
        if (errnoName == null) {
            errnoName = "errno " + mErrno;
        }
        String description = Syscalls.strerror(mErrno);
        return mFunctionName + " failed: " + errnoName + " (" + description + ")";
    }

    private static class ErrnoExceptionCompat {

        static {
            ReflectedAccessor.allowRestrictedHiddenApiAccess();
        }

        @RestrictedHiddenApi
        private static final ReflectedField sFunctionNameField = new ReflectedField(
                ErrnoException.class, "functionName");

        private ErrnoExceptionCompat() {}

        @NonNull
        public static String getFunctionName(@NonNull ErrnoException errnoException) {
            return sFunctionNameField.getObject(errnoException);
        }
    }
}
