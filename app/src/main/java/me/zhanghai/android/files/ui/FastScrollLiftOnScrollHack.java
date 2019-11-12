/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui;

import com.google.android.material.appbar.AppBarLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class FastScrollLiftOnScrollHack {

    private FastScrollLiftOnScrollHack() {}

    public static void hack(@NonNull AppBarLayout appBarLayout) {
        appBarLayout.getViewTreeObserver().addOnPreDrawListener(() -> {
            // Call AppBarLayout.Behavior.onNestedPreScroll() with dy == 0 to update lifted state.
            CoordinatorLayout.Behavior behavior =
                    ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).getBehavior();
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) appBarLayout.getParent();
            behavior.onNestedPreScroll(coordinatorLayout, appBarLayout, coordinatorLayout, 0, 0,
                    null, 0);
            return true;
        });
    }
}
