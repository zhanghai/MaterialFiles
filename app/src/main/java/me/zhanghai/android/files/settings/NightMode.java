/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import androidx.appcompat.app.AppCompatDelegate;

public enum NightMode {

    // Disabled because AppCompatDelegate delegates night mode change to the non-existent system
    // implementation.
    FOLLOW_SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    OFF(AppCompatDelegate.MODE_NIGHT_NO),
    ON(AppCompatDelegate.MODE_NIGHT_YES),
    AUTO(AppCompatDelegate.MODE_NIGHT_AUTO);

    private int value;

    NightMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
