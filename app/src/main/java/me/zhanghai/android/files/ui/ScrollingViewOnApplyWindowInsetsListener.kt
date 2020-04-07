/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.graphics.Rect
import android.view.View
import android.view.WindowInsets
import me.zhanghai.android.fastscroll.FastScroller

class ScrollingViewOnApplyWindowInsetsListener(
    view: View,
    private val fastScroller: FastScroller
) : View.OnApplyWindowInsetsListener {
    private val initialPadding =
        Rect(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)

    init {
        fastScroller.setPadding(0, 0, 0, 0)
    }

    override fun onApplyWindowInsets(view: View, insets: WindowInsets): WindowInsets {
        view.setPadding(
            initialPadding.left + insets.systemWindowInsetLeft, initialPadding.top,
            initialPadding.right + insets.systemWindowInsetRight,
            initialPadding.bottom + insets.systemWindowInsetBottom
        )
        fastScroller.setPadding(
            insets.systemWindowInsetLeft, 0, insets.systemWindowInsetRight,
            insets.systemWindowInsetBottom
        )
        return insets
    }
}
