/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package androidx.appcompat.app;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class AppCompatDelegateCompat {

    private AppCompatDelegateCompat() {}

    @SuppressLint("RestrictedApi")
    public static int mapNightMode(@NonNull AppCompatDelegate delegate, int mode) {
        return ((AppCompatDelegateImpl) delegate).mapNightMode(mode);
    }
}
