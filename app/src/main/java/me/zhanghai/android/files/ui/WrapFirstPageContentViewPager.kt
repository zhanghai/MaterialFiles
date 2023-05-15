/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager

class WrapFirstPageContentViewPager : ViewPager {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val newHeightMeasureSpec = if (heightMode != MeasureSpec.EXACTLY && childCount > 0) {
            val child = getChildAt(0)
            child.measure(widthMeasureSpec, heightMeasureSpec)
            val height = child.measuredHeight
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        } else {
            heightMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }
}
