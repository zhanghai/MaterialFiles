/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.util.AttributeSet;

/**
 * A {@link NavigationView} that draws no scrim and dispatches window insets correctly.
 * <p>
 * {@code android:fitsSystemWindows="true"} will be ignored.
 * </p>
 */
public class FullscreenNavigationView extends NavigationView {

    public FullscreenNavigationView(Context context) {
        super(context);

        init();
    }

    public FullscreenNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public FullscreenNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        // Required to revert the value of fitsSystemWindows set in super constructor.
        setFitsSystemWindows(false);
        setOnApplyWindowInsetsListener((view, insets) -> {
            if (getHeaderCount() == 0) {
                return FullscreenNavigationView.this.onApplyWindowInsets(insets);
            }
            return getHeaderView(0).onApplyWindowInsets(insets);
        });
    }
}
