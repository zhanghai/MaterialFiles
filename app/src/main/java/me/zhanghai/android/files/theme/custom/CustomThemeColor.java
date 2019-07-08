/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.theme.custom;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

public interface CustomThemeColor {

    @ColorRes
    int getResourceId();

    @NonNull
    String getResourceEntryName();
}
