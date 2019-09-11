/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.content.pm.ApplicationInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import me.zhanghai.java.reflected.ReflectedField;

public class ApplicationInfoCompat {

    static {
        RestrictedHiddenApiAccess.allow();
    }

    @RestrictedHiddenApi
    private static final ReflectedField<ApplicationInfo> sVersionCodeField = new ReflectedField<>(
            ApplicationInfo.class, "versionCode");

    @RestrictedHiddenApi
    private static final ReflectedField<ApplicationInfo> sLongVersionCodeField =
            new ReflectedField<>(ApplicationInfo.class, "longVersionCode");

    private ApplicationInfoCompat() {}

    public static long getLongVersionCode(@NonNull ApplicationInfo applicationInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return sLongVersionCodeField.getLong(applicationInfo);
        } else {
            return sVersionCodeField.getInt(applicationInfo);
        }
    }
}
