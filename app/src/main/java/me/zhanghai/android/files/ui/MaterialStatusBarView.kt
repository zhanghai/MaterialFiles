/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.MaterialShapeUtils
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.getColorStateListByAttr

class MaterialStatusBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    private var insets: WindowInsets? = null

    init {
        fitsSystemWindows = true
        background = MaterialShapeDrawable().apply {
            fillColor = context.getColorStateListByAttr(R.attr.colorSurface)
            initializeElevationOverlay(context)
            elevation = this@MaterialStatusBarView.elevation
        }
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets =
        insets.also { this.insets = it }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        MaterialShapeUtils.setParentAbsoluteElevation(this)
    }

    override fun setElevation(elevation: Float) {
        super.setElevation(elevation)

        MaterialShapeUtils.setElevation(this, elevation)
    }

    @SuppressLint("MissingSuperCall")
    override fun draw(canvas: Canvas) {
        val insets = insets ?: return
        background.setBounds(insets.systemWindowInsetLeft, 0,
            width - insets.systemWindowInsetLeft - insets.systemWindowInsetRight,
            insets.systemWindowInsetTop)
        background.draw(canvas)
    }
}
