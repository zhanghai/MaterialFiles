/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import me.zhanghai.android.files.util.fadeInUnsafe
import me.zhanghai.android.files.util.fadeOutUnsafe

class OverlayToolbarActionMode(
    toolbar: Toolbar,
    private val overlaidLayout: ViewGroup
) : ToolbarActionMode(toolbar, toolbar) {
    init {
        toolbar.isVisible = false
    }

    override fun show(bar: ViewGroup, animate: Boolean) {
        if (animate) {
            bar.fadeInUnsafe()
        } else {
            bar.isVisible = true
        }
        overlaidLayout.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
    }

    override fun hide(bar: ViewGroup, animate: Boolean) {
        if (animate) {
            bar.fadeOutUnsafe()
        } else {
            bar.isVisible = false
        }
        overlaidLayout.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
    }
}
