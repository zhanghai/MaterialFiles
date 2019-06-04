/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.system.ErrnoException;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.reflected.ReflectedAccessor;
import me.zhanghai.android.files.reflected.ReflectedField;
import me.zhanghai.android.files.reflected.RestrictedHiddenApi;

public class ErrnoExceptionCompat {

    static {
        ReflectedAccessor.allowRestrictedHiddenApiAccess();
    }

    @RestrictedHiddenApi
    private static final ReflectedField<ErrnoException> sFunctionNameField = new ReflectedField<>(
            ErrnoException.class, "functionName");

    private ErrnoExceptionCompat() {}

    @NonNull
    public static String getFunctionName(@NonNull ErrnoException errnoException) {
        return sFunctionNameField.getObject(errnoException);
    }
}
