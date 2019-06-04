/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.os.Build;

import androidx.annotation.NonNull;
import me.zhanghai.java.reflected.ReflectedAccessor;
import me.zhanghai.java.reflected.ReflectedField;

public class RestrictedHiddenApiAccess {

    @NonNull
    private static final ReflectedField<Class> sClassClassLoaderField = new ReflectedField<>(
            Class.class, "classLoader");

    private static boolean sAllowed;
    @NonNull
    private static final Object sAllowedLock = new Object();

    public static void allow() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }
        synchronized (sAllowedLock) {
            if (!sAllowed) {
                sClassClassLoaderField.setObject(ReflectedAccessor.class, null);
                sAllowed = true;
            }
        }
    }
}
