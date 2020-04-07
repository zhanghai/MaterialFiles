/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

// @see https://github.com/chrisbanes/PhotoView/blob/master/sample/src/main/java/com/github/chrisbanes/photoview/sample/HackyViewPager.java
class FixIllegalArgumentExceptionViewPager : ViewPager {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean =
        try {
            super.onInterceptTouchEvent(event)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            false
        }
}
