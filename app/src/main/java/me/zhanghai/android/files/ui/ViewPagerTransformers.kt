/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.view.View
import androidx.viewpager.widget.ViewPager
import kotlin.math.absoluteValue

// @see https://developer.android.com/training/animation/screen-slide

object DepthPageTransformer : ViewPager.PageTransformer {
    private const val MIN_SCALE = 0.75f

    override fun transformPage(view: View, position: Float) {
        val pageWidth = view.width
        when {
            // [-Infinity,-1)
            // This page is way off-screen to the left.
            position < -1 -> view.alpha = 0f
            // [-1,0]
            position <= 0 -> {
                // Use the default slide transition when moving to the left page
                view.alpha = 1f
                view.translationX = 0f
                view.scaleX = 1f
                view.scaleY = 1f
            }
            // (0,1]
            position <= 1 -> {
                // Fade the page out.
                view.alpha = 1 - position
                // Counteract the default slide transition
                view.translationX = pageWidth * -position
                // Scale the page down (between MIN_SCALE and 1)
                val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - position.absoluteValue)
                view.scaleX = scaleFactor
                view.scaleY = scaleFactor
            }
            // (1,+Infinity]
            // This page is way off-screen to the right.
            else -> view.alpha = 0f
        }
    }
}

object ZoomOutPageTransformer : ViewPager.PageTransformer {
    private const val MIN_SCALE = 0.85f
    private const val MIN_ALPHA = 0.5f

    override fun transformPage(view: View, position: Float) {
        val pageWidth = view.width
        val pageHeight = view.height
        when {
            // [-Infinity,-1)
            // This page is way off-screen to the left.
            position < -1 -> view.alpha = 0f
            // [-1,1]
            position <= 1 -> {
                // Modify the default slide transition to shrink the page as well
                val scaleFactor = (1 - position.absoluteValue).coerceAtLeast(MIN_SCALE)
                val verticalMargin = pageHeight * (1 - scaleFactor) / 2
                val horizontalMargin = pageWidth * (1 - scaleFactor) / 2
                if (position < 0) {
                    view.translationX = horizontalMargin - verticalMargin / 2
                } else {
                    view.translationX = -horizontalMargin + verticalMargin / 2
                }
                // Scale the page down (between MIN_SCALE and 1)
                view.scaleX = scaleFactor
                view.scaleY = scaleFactor
                // Fade the page relative to its size.
                view.alpha = (MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)
                    * (1 - MIN_ALPHA))
            }
            // (1,+Infinity]
            // This page is way off-screen to the right.
            else -> view.alpha = 0f
        }
    }
}
