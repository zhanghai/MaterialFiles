/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.colorpicker

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.appcompat.content.res.AppCompatResources
import me.zhanghai.android.files.R
import me.zhanghai.android.files.ui.CheckableView

class ColorSwatchView : CheckableView {
    private val gradientDrawable: GradientDrawable

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        val background = AppCompatResources.getDrawable(
            context, R.drawable.color_swatch_view_background
        ) as LayerDrawable
        gradientDrawable = background.getDrawable(0) as GradientDrawable
        setBackground(background)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            resolveSize(suggestedMinimumWidth, widthMeasureSpec),
            resolveSize(suggestedMinimumHeight, heightMeasureSpec)
        )
    }

    fun setColor(@ColorInt color: Int) {
        gradientDrawable.apply {
            mutate()
            setColor(color)
        }
    }
}
