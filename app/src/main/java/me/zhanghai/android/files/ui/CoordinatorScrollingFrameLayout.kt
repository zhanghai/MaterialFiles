/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior
import androidx.core.view.ScrollingView
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.google.android.material.appbar.AppBarLayout.ScrollingViewBehavior
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.layoutInNavigation
import me.zhanghai.android.files.util.valueCompat

class CoordinatorScrollingFrameLayout : FrameLayout, AttachedBehavior {
    private var lastInsets: WindowInsets? = null

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
        fitsSystemWindows = true
        if (Settings.MATERIAL_DESIGN_2.valueCompat
            && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            layoutInNavigation = true
        }
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets =
        insets.also { lastInsets = it }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val lastInsets = lastInsets
        if (lastInsets != null) {
            val scrollingView = findScrollingView()
            val scrollingChildView = scrollingView?.let { findChildView(it) }
            for (index in 0 until childCount) {
                val childView = getChildAt(index)
                if (childView != scrollingChildView) {
                    childView.updateLayoutParams<MarginLayoutParams> {
                        leftMargin = lastInsets.systemWindowInsetLeft
                        rightMargin = lastInsets.systemWindowInsetRight
                        topMargin = 0
                        bottomMargin = lastInsets.systemWindowInsetBottom
                    }
                }
            }
            if (scrollingView != null) {
                if (scrollingView.fitsSystemWindows) {
                    scrollingView.onApplyWindowInsets(lastInsets)
                } else {
                    scrollingView.updatePadding(
                        left = lastInsets.systemWindowInsetLeft,
                        right = lastInsets.systemWindowInsetRight,
                        bottom = lastInsets.systemWindowInsetBottom
                    )
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun findScrollingView(viewGroup: ViewGroup = this): View? {
        for (index in 0 until viewGroup.childCount) {
            val view = viewGroup.getChildAt(index)
            if (view is ScrollingView) {
                return view
            }
            if (view is ViewGroup) {
                findScrollingView(view)?.let { return it }
            }
        }
        return null
    }

    private fun findChildView(view: View): View? {
        var view = view
        while (true) {
            val parent = view.parent
            if (parent == this) {
                return view
            }
            if (parent !is View) {
                return null
            }
            view = parent
        }
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> = Behavior()

    private class Behavior : ScrollingViewBehavior() {
        override fun onMeasureChild(
            parent: CoordinatorLayout,
            child: View,
            parentWidthMeasureSpec: Int,
            widthUsed: Int,
            parentHeightMeasureSpec: Int,
            heightUsed: Int
        ): Boolean {
            var parentHeightMeasureSpec = parentHeightMeasureSpec
            @SuppressLint("RestrictedApi")
            val parentInsets = parent.lastWindowInsets
            if (parentInsets != null) {
                var parentHeightSize = MeasureSpec.getSize(parentHeightMeasureSpec)
                parentHeightSize -= parentInsets.systemWindowInsetTop
                val parentHeightMode = MeasureSpec.getMode(parentHeightMeasureSpec)
                parentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    parentHeightSize, parentHeightMode
                )
            }
            return super.onMeasureChild(
                parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec,
                heightUsed
            )
        }
    }
}
