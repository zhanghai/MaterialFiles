/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout

fun AppBarLayout.updateLiftOnScrollOnPreDraw() {
    val consumed = IntArray(2)
    viewTreeObserver.addOnPreDrawListener {
        // Call AppBarLayout.Behavior.onNestedPreScroll() with dy == 0 to update lifted state.
        val behavior = (layoutParams as CoordinatorLayout.LayoutParams).behavior!!
        val coordinatorLayout = parent as CoordinatorLayout
        behavior.onNestedPreScroll(coordinatorLayout, this, coordinatorLayout, 0, 0, consumed, 0)
        true
    }
}
