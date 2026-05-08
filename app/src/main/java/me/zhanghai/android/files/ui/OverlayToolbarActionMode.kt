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
    bar: ViewGroup,
    toolbar: Toolbar,
    private val siblingView: ViewGroup? = null
) : ToolbarActionMode(bar, toolbar) {
    constructor(toolbar: Toolbar) : this(toolbar, toolbar)

    constructor(toolbar: Toolbar, siblingView: ViewGroup) : this(toolbar, toolbar, siblingView)

    init {
        bar.isVisible = false
    }

    override fun show(bar: ViewGroup, animate: Boolean) {
        siblingView?.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        if (animate) {
            bar.fadeInUnsafe()
        } else {
            bar.isVisible = true
        }
    }

    override fun hide(bar: ViewGroup, animate: Boolean) {
        siblingView?.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
        if (animate) {
            bar.fadeOutUnsafe()
        } else {
            bar.isVisible = false
        }
    }
}
