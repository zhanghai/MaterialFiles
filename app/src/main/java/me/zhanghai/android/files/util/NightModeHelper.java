/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import androidx.appcompat.app.AppCompatDelegate;
import me.zhanghai.android.files.settings.SettingsLiveDatas;

public class NightModeHelper {

    private NightModeHelper() {}

    public static void syncDefaultNightMode() {
        int nightModeValue = SettingsLiveDatas.NIGHT_MODE.getValue().getValue();
        AppCompatDelegate.setDefaultNightMode(nightModeValue);
    }
}
