/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package com.google.android.material.appbar;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowInsetsCompat;

public class OnWindowInsetChangedAppBarLayout extends AppBarLayout {
    public OnWindowInsetChangedAppBarLayout(@NonNull Context context) {
        super(context);
    }

    public OnWindowInsetChangedAppBarLayout(@NonNull Context context,
                                            @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OnWindowInsetChangedAppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                                            @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public WindowInsetsCompat onWindowInsetChanged(@NonNull WindowInsetsCompat insets) {
        return super.onWindowInsetChanged(insets);
    }
}
