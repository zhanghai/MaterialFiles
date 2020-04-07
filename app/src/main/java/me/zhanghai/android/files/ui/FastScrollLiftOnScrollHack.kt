/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout

object FastScrollLiftOnScrollHack {
    fun hack(appBarLayout: AppBarLayout) {
        appBarLayout.viewTreeObserver.addOnPreDrawListener {
            // Call AppBarLayout.Behavior.onNestedPreScroll() with dy == 0 to update lifted state.
            val behavior = (appBarLayout.layoutParams as CoordinatorLayout.LayoutParams).behavior!!
            val coordinatorLayout = appBarLayout.parent as CoordinatorLayout
            behavior.onNestedPreScroll(
                coordinatorLayout, appBarLayout, coordinatorLayout, 0, 0, intArrayOf(0, 0), 0
            )
            true
        }
    }
}
