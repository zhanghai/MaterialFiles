/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PassWindowInsetsFrameLayout extends FrameLayout {

    public PassWindowInsetsFrameLayout(@NonNull Context context) {
        super(context);
    }

    public PassWindowInsetsFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PassWindowInsetsFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                                       int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PassWindowInsetsFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                                       int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @NonNull
    @Override
    public WindowInsets onApplyWindowInsets(@NonNull WindowInsets insets) {
        return insets;
    }
}
