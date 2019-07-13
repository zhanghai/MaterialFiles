/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.ViewUtils;

public class StatusBarBackgroundView extends View {

    @NonNull
    private Drawable mStatusBarBackground;

    @Nullable
    private WindowInsets mInsets;

    public StatusBarBackgroundView(@NonNull Context context) {
        super(context);

        init();
    }

    public StatusBarBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public StatusBarBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs,
                                   int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public StatusBarBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs,
                                   int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {

        int darkPrimaryColor = ViewUtils.getColorFromAttrRes(R.attr.colorPrimaryDark, 0,
                getContext());
        mStatusBarBackground = new ColorDrawable(darkPrimaryColor);

        @SuppressLint("PrivateResource")
        float appBarElevation = getResources().getDimension(R.dimen.design_appbar_elevation);
        setElevation(appBarElevation);
        setFitsSystemWindows(true);
        setWillNotDraw(true);
    }

    @NonNull
    @Override
    public WindowInsets onApplyWindowInsets(@NonNull WindowInsets insets) {
        mInsets = insets;
        setWillNotDraw(mInsets.getSystemWindowInsetTop() == 0);
        return insets;
    }

    @Override
    @SuppressLint("MissingSuperCall")
    public void draw(@NonNull Canvas canvas) {
        if (mInsets == null) {
            return;
        }
        int left = mInsets.getSystemWindowInsetLeft();
        int right = getWidth() - left - mInsets.getSystemWindowInsetRight();
        mStatusBarBackground.setBounds(left, 0, right, mInsets.getSystemWindowInsetTop());
        mStatusBarBackground.draw(canvas);
    }
}
