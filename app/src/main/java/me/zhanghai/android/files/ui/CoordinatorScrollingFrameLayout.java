/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import com.google.android.material.appbar.AppBarLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ScrollingView;
import androidx.core.view.WindowInsetsCompat;
import me.zhanghai.android.files.settings.Settings;
import me.zhanghai.android.files.util.ViewUtils;

public class CoordinatorScrollingFrameLayout extends FrameLayout
        implements CoordinatorLayout.AttachedBehavior {

    @Nullable
    private WindowInsets mLastInsets;

    public CoordinatorScrollingFrameLayout(@NonNull Context context) {
        super(context);

        init();
    }

    public CoordinatorScrollingFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public CoordinatorScrollingFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                                           @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public CoordinatorScrollingFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                                           @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        setFitsSystemWindows(true);
        if (Settings.MATERIAL_DESIGN_2.getValue()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            ViewUtils.setLayoutHideNavigation(this);
        }
    }

    @NonNull
    @Override
    public WindowInsets onApplyWindowInsets(@NonNull WindowInsets insets) {
        mLastInsets = insets;
        return insets;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mLastInsets != null) {
            View scrollingView = findScrollingView();
            View scrollingChildView = scrollingView != null ? findChildView(scrollingView) : null;
            for (int i = 0; i < getChildCount(); ++i) {
                View childView = getChildAt(i);
                if (childView != scrollingChildView) {
                    ViewUtils.setMargin(childView, mLastInsets.getSystemWindowInsetLeft(),
                            0, mLastInsets.getSystemWindowInsetRight(),
                            mLastInsets.getSystemWindowInsetBottom());
                }
            }
            if (scrollingView != null) {
                if (scrollingView.getFitsSystemWindows()) {
                    scrollingView.onApplyWindowInsets(mLastInsets);
                } else {
                    scrollingView.setPadding(mLastInsets.getSystemWindowInsetLeft(),
                            scrollingView.getPaddingTop(),
                            mLastInsets.getSystemWindowInsetRight(),
                            mLastInsets.getSystemWindowInsetBottom());
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Nullable
    private View findChildView(@NonNull View view) {
        while (true) {
            ViewParent parent = view.getParent();
            if (parent == this) {
                return view;
            }
            if (!(parent instanceof View)) {
                return null;
            }
            view = (View) parent;
        }
    }

    @Nullable
    private View findScrollingView() {
        return findScrollingView(this);
    }

    @Nullable
    private static View findScrollingView(@NonNull ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); ++i) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof ScrollingView) {
                return view;
            }
            if (view instanceof ViewGroup) {
                View scrollingView = findScrollingView((ViewGroup) view);
                if (scrollingView != null) {
                    return scrollingView;
                }
            }
        }
        return null;
    }

    @NonNull
    @Override
    public CoordinatorLayout.Behavior getBehavior() {
        return new Behavior();
    }

    private static class Behavior extends AppBarLayout.ScrollingViewBehavior {

        @Override
        public boolean onMeasureChild(@NonNull CoordinatorLayout parent, @NonNull View child,
                                      int parentWidthMeasureSpec, int widthUsed,
                                      int parentHeightMeasureSpec, int heightUsed) {
            WindowInsetsCompat parentInsets = parent.getLastWindowInsets();
            if (parentInsets != null) {
                int parentHeightSize = MeasureSpec.getSize(parentHeightMeasureSpec);
                parentHeightSize -= parentInsets.getSystemWindowInsetTop();
                int parentHeightMode = MeasureSpec.getMode(parentHeightMeasureSpec);
                parentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(parentHeightSize,
                        parentHeightMode);
            }
            return super.onMeasureChild(parent, child, parentWidthMeasureSpec, widthUsed,
                    parentHeightMeasureSpec, heightUsed);
        }
    }
}
