/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.system.ErrnoException;

import androidx.annotation.NonNull;
import me.zhanghai.java.reflected.ReflectedField;

public class ErrnoExceptionCompat {

    static {
        RestrictedHiddenApiAccess.allow();
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
