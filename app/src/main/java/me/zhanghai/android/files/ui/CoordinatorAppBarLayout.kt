/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.ColorUtils
import com.google.android.material.appbar.AppBarLayout
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.activity
import me.zhanghai.android.files.util.getColorByAttr

class CoordinatorAppBarLayout : AppBarLayout {
    private val tempConsumed = IntArray(2)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    init {
        fitsSystemWindows = true

        val appBarSurfaceColor = context.getColorByAttr(R.attr.colorAppBarSurface)
        val primaryDarkColor = context.getColorByAttr(R.attr.colorPrimaryDark)
        if (primaryDarkColor == appBarSurfaceColor
            || ColorUtils.setAlphaComponent(primaryDarkColor, 255) == appBarSurfaceColor) {
            context.activity!!.window.statusBarColor = Color.TRANSPARENT
        }
    }

    override fun draw(canvas: Canvas) {
        if (isLiftOnScroll) {
            // Call AppBarLayout.Behavior.onNestedPreScroll() with dy == 0 to update lifted state.
            val behavior = (layoutParams as CoordinatorLayout.LayoutParams).behavior!!
            val coordinatorLayout = parent as CoordinatorLayout
            behavior.onNestedPreScroll(
                coordinatorLayout, this, coordinatorLayout, 0, 0, tempConsumed, 0
            )
        }

        super.draw(canvas)
    }
}
