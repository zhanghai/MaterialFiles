/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.foregroundcompat;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

interface ForegroundCompatView {

    @Nullable
    Drawable getSupportForeground();

    void setSupportForeground(@Nullable Drawable foreground);

    int getSupportForegroundGravity();

    void setSupportForegroundGravity(int gravity);

    void setSupportForegroundTintList(@Nullable ColorStateList tint);

    @Nullable
    ColorStateList getSupportForegroundTintList();

    void setSupportForegroundTintMode(@Nullable PorterDuff.Mode tintMode);

    @Nullable
    PorterDuff.Mode getSupportForegroundTintMode();
}
