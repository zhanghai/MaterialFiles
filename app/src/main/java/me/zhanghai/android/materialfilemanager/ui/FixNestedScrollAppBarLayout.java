/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

public class FixNestedScrollAppBarLayout extends AppBarLayout
        implements CoordinatorLayout.AttachedBehavior {

    public FixNestedScrollAppBarLayout(Context context) {
        super(context);
    }

    public FixNestedScrollAppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @NonNull
    @Override
    public CoordinatorLayout.Behavior getBehavior() {
        return new Behavior();
    }

    // https://gist.github.com/chrisbanes/8391b5adb9ee42180893300850ed02f2
    // Workaround for https://issuetracker.google.com/66996774
    private static class Behavior extends AppBarLayout.Behavior {

        public Behavior() {}

        public Behavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child,
                                   View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                                   int dyUnconsumed, int type) {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed,
                    dxUnconsumed, dyUnconsumed, type);
            stopNestedScrollIfNeeded(dyUnconsumed, child, target, type);
        }

        @Override
        public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child,
                                      View target, int dx, int dy, int[] consumed, int type) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
            stopNestedScrollIfNeeded(dy, child, target, type);
        }

        private void stopNestedScrollIfNeeded(int dy, AppBarLayout child, View target, int type) {
            if (type == ViewCompat.TYPE_NON_TOUCH) {
                int currOffset = getTopAndBottomOffset();
                if ((dy < 0 && currOffset == 0) || (dy > 0
                        && currOffset == -child.getTotalScrollRange())) {
                    ViewCompat.stopNestedScroll(target, ViewCompat.TYPE_NON_TOUCH);
                }
            }
        }
    }
}
