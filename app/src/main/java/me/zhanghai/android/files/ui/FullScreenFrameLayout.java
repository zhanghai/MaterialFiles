/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import me.zhanghai.android.files.util.ViewUtils;

public class FullScreenFrameLayout extends FrameLayout {

    public FullScreenFrameLayout(@NonNull Context context) {
        super(context);

        init();
    }

    public FullScreenFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public FullScreenFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                                 @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public FullScreenFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                                 @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        ViewUtils.setLayoutFullscreen(this);
    }

    @Override
    public WindowInsets dispatchApplyWindowInsets(@NonNull WindowInsets insets) {
        insets = onApplyWindowInsets(insets);
        if (insets.isConsumed()) {
            return insets;
        }
        for (int i = 0, childCount = getChildCount(); i < childCount; ++i) {
            View child = getChildAt(i);
            child.dispatchApplyWindowInsets(insets);
        }
        return insets.consumeSystemWindowInsets();
    }
}
