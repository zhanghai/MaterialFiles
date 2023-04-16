/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.WindowInsets
import androidx.annotation.AttrRes
import androidx.core.graphics.withSave
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.activity
import me.zhanghai.android.files.util.displayWidth
import me.zhanghai.android.files.util.getDimensionPixelSize
import me.zhanghai.android.files.util.getDimensionPixelSizeByAttr
import me.zhanghai.android.files.util.getDrawableByAttr
import me.zhanghai.android.files.util.isLayoutDirectionRtl

class NavigationRecyclerView : RecyclerView {
    private val verticalPadding = context.getDimensionPixelSize(
        com.google.android.material.R.dimen.design_navigation_padding_bottom
    )
    private val actionBarSize =
        context.getDimensionPixelSizeByAttr(androidx.appcompat.R.attr.actionBarSize)
    private val maxWidth = context.getDimensionPixelSize(R.dimen.navigation_max_width)
    private var scrim = context.getDrawableByAttr(android.R.attr.statusBarColor)

    private var insetStart = 0
    private var insetTop = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    init {
        updatePadding(top = verticalPadding, bottom = verticalPadding)
        fitsSystemWindows = true
        setWillNotDraw(false)
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var widthSpec = widthSpec
        var width = (context.displayWidth - actionBarSize).coerceIn(0..insetStart + maxWidth)
        when (MeasureSpec.getMode(widthSpec)) {
            MeasureSpec.AT_MOST -> {
                width = width.coerceAtMost(MeasureSpec.getSize(widthSpec))
                widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
            }
            MeasureSpec.UNSPECIFIED ->
                widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
            MeasureSpec.EXACTLY -> {}
        }
        super.onMeasure(widthSpec, heightSpec)
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        val isLayoutDirectionRtl = isLayoutDirectionRtl
        insetStart = if (isLayoutDirectionRtl) {
            insets.systemWindowInsetRight
        } else {
            insets.systemWindowInsetLeft
        }
        val paddingLeft = if (isLayoutDirectionRtl) 0 else insetStart
        val paddingRight = if (isLayoutDirectionRtl) insetStart else 0
        insetTop = insets.systemWindowInsetTop
        setPadding(
            paddingLeft, verticalPadding + insetTop, paddingRight,
            verticalPadding + insets.systemWindowInsetBottom
        )
        requestLayout()
        return insets.replaceSystemWindowInsets(
            insets.systemWindowInsetLeft - paddingLeft, 0,
            insets.systemWindowInsetRight - paddingRight, 0
        )
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        if (context.activity!!.window.statusBarColor == Color.TRANSPARENT) {
            canvas.withSave {
                canvas.translate(scrollX.toFloat(), scrollY.toFloat())
                scrim.setBounds(0, 0, width, insetTop)
                scrim.draw(canvas)
            }
        }
    }
}
