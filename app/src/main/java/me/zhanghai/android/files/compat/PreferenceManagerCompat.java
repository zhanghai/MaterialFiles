/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.content.Context;

import androidx.annotation.NonNull;

public class PreferenceManagerCompat {

    private PreferenceManagerCompat() {}

    @NonNull
    public static String getDefaultSharedPreferencesName(@NonNull Context context) {
        return context.getPackageName() + "_preferences";
    }

    public static int getDefaultSharedPreferencesMode() {
        return Context.MODE_PRIVATE;
    }
}
