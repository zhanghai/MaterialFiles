/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import me.zhanghai.java.reflected.ReflectedMethod;

public class MoreContextCompat {

    static {
        RestrictedHiddenApiAccess.allow();
    }

    @RestrictedHiddenApi
    private static final ReflectedMethod<Context> sGetThemeResIdMethod = new ReflectedMethod<>(
            Context.class, "getThemeResId");

    private MoreContextCompat() {}

    @StyleRes
    public static int getThemeResId(@NonNull Context context) {
        return sGetThemeResIdMethod.invoke(context);
    }
}
