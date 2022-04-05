/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.shape.MaterialShapeDrawable

class OverlayToolbar : MaterialToolbar {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // @see MaterialShapeUtils.setParentAbsoluteElevation
        val background = background
        if (background is MaterialShapeDrawable && background.isElevationOverlayEnabled) {
            // This fix is needed for elevation overlay after activity recreation on S+.
            background.parentAbsoluteElevation = 0f
        }
    }
}
