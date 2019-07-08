/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import me.zhanghai.java.reflected.ReflectedMethod;

public class ContextCompat {

    static {
        RestrictedHiddenApiAccess.allow();
    }

    @RestrictedHiddenApi
    private static final ReflectedMethod<Context> sGetThemeResIdMethod = new ReflectedMethod<>(
            Context.class, "getThemeResId");

    private ContextCompat() {}

    @StyleRes
    public static int getThemeResId(@NonNull Context context) {
        return sGetThemeResIdMethod.invoke(context);
    }
}
