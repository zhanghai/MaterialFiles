/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.getColorByAttr
import me.zhanghai.android.files.util.getDimension

class StatusBarBackgroundView : View {
    private var statusBarBackground = ColorDrawable(context.getColorByAttr(R.attr.colorPrimaryDark))

    private var insets: WindowInsets? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        @SuppressLint("PrivateResource")
        elevation = context.getDimension(R.dimen.design_appbar_elevation)
        fitsSystemWindows = true
        setWillNotDraw(true)
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets =
        insets.also {
            this.insets = it
            setWillNotDraw(it.systemWindowInsetTop == 0)
        }

    @SuppressLint("MissingSuperCall")
    override fun draw(canvas: Canvas) {
        val insets = insets ?: return
        statusBarBackground.setBounds(insets.systemWindowInsetLeft, 0,
            width - insets.systemWindowInsetLeft - insets.systemWindowInsetRight,
            insets.systemWindowInsetTop)
        statusBarBackground.draw(canvas)
    }
}
