/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.foregroundcompat;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

interface ForegroundCompatView {

    @Nullable
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    Drawable getSupportForeground();

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    void setSupportForeground(@Nullable Drawable foreground);

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    int getSupportForegroundGravity();

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    void setSupportForegroundGravity(int gravity);

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    void setSupportForegroundTintList(@Nullable ColorStateList tint);

    @Nullable
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    ColorStateList getSupportForegroundTintList();

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    void setSupportForegroundTintMode(@Nullable PorterDuff.Mode tintMode);

    @Nullable
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    PorterDuff.Mode getSupportForegroundTintMode();
}
