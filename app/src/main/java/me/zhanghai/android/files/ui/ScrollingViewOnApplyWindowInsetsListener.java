/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.graphics.Rect;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.fastscroll.FastScroller;

public class ScrollingViewOnApplyWindowInsetsListener implements View.OnApplyWindowInsetsListener {

    @NonNull
    private final Rect mPadding = new Rect();
    @Nullable
    private final FastScroller mFastScroller;

    public ScrollingViewOnApplyWindowInsetsListener(@Nullable View view,
                                                    @Nullable FastScroller fastScroller) {
        if (view != null) {
            mPadding.set(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(),
                    view.getPaddingBottom());
        }
        mFastScroller = fastScroller;
        if (mFastScroller != null) {
            // Prevent FastScroller from using view padding even if no window insets is dispatched.
            mFastScroller.setPadding(0, 0, 0, 0);
        }
    }

    public ScrollingViewOnApplyWindowInsetsListener() {
        this(null, null);
    }

    @NonNull
    @Override
    public WindowInsets onApplyWindowInsets(@NonNull View view, @NonNull WindowInsets insets) {
        view.setPadding(mPadding.left + insets.getSystemWindowInsetLeft(), mPadding.top,
                mPadding.right + insets.getSystemWindowInsetRight(),
                mPadding.bottom + insets.getSystemWindowInsetBottom());
        if (mFastScroller != null) {
            mFastScroller.setPadding(insets.getSystemWindowInsetLeft(), 0,
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
        }
        return insets;
    }
}
