/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.os.Build;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.reflected.ReflectedAccessor;
import me.zhanghai.android.files.reflected.ReflectedField;

public class RestrictedHiddenApiAccess {

    @NonNull
    private static final ReflectedField<Class> sClassClassLoaderField = new ReflectedField<>(
            Class.class, "classLoader");

    @NonNull
    private static final Object sRestrictedHiddenApiAccessAllowedLock = new Object();

    private static boolean sRestrictedHiddenApiAccessAllowed;

    public static void allow() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }
        synchronized (sRestrictedHiddenApiAccessAllowedLock) {
            if (!sRestrictedHiddenApiAccessAllowed) {
                sClassClassLoaderField.setObject(ReflectedAccessor.class, null);
                sRestrictedHiddenApiAccessAllowed = true;
            }
        }
    }
}
