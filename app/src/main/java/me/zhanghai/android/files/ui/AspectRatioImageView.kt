/*
 * Copyright (c) 2023 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import com.google.android.material.imageview.ShapeableImageView
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.obtainStyledAttributesCompat
import me.zhanghai.android.files.compat.use
import kotlin.math.roundToInt

open class AspectRatioImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {
    @SuppressLint("RestrictedApi")
    var ratio: Float = context.obtainStyledAttributesCompat(
        attrs, R.styleable.AspectRatioFrameLayout, defStyleAttr
    ).use { it.getFloat(R.styleable.AspectRatioFrameLayout_aspectRatio, 0f) }
        set(value) {
            if (field == value) {
                return
            }
            field = value
            requestLayout()
            invalidate()
        }

    fun setRatio(width: Float, height: Float) {
        ratio = width / height
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newWidthMeasureSpec: Int
        val newHeightMeasureSpec: Int
        if (ratio > 0) {
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            if (widthMode == MeasureSpec.EXACTLY) {
                val width = MeasureSpec.getSize(widthMeasureSpec)
                val height = (width / ratio).roundToInt().coerceIn(minimumHeight, maxHeight)
                newWidthMeasureSpec = widthMeasureSpec
                newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            } else {
                val height = MeasureSpec.getSize(heightMeasureSpec)
                val width = (ratio * height).roundToInt().coerceIn(minimumWidth, maxWidth)
                newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
                newHeightMeasureSpec = heightMeasureSpec
            }
        } else {
            newWidthMeasureSpec = widthMeasureSpec
            newHeightMeasureSpec = heightMeasureSpec
        }
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
    }
}
