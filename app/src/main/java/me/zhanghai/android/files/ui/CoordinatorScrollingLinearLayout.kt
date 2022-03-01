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
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior
import androidx.core.graphics.Insets
import androidx.core.view.ScrollingView
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.google.android.material.appbar.AppBarLayout.ScrollingViewBehavior
import me.zhanghai.android.files.util.layoutInNavigation

class CoordinatorScrollingLinearLayout : LinearLayout, AttachedBehavior {
    private var bottomInsets: WindowInsets? = null

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
        orientation = VERTICAL
        fitsSystemWindows = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            layoutInNavigation = true
        }
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        updatePadding(left = insets.systemWindowInsetLeft, right = insets.systemWindowInsetRight)
        bottomInsets = WindowInsetsCompat.Builder()
            .setSystemWindowInsets(Insets.of(0, 0, 0, insets.systemWindowInsetBottom))
            .build()
            .toWindowInsets()
        requestLayout()
        return insets
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val bottomInsets = bottomInsets
        if (bottomInsets != null) {
            val childView = getChildAt(childCount - 1)
            if (childView != null) {
                val scrollingView = findScrollingView()
                val scrollingChildView = scrollingView?.let { findChildView(it) }
                if (childView == scrollingChildView) {
                    if (scrollingView.fitsSystemWindows) {
                        scrollingView.onApplyWindowInsets(bottomInsets)
                    } else {
                        scrollingView.updatePadding(bottom = bottomInsets.systemWindowInsetBottom)
                    }
                } else {
                    childView.updateLayoutParams<MarginLayoutParams> {
                        bottomMargin = bottomInsets.systemWindowInsetBottom
                    }
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
                val parentHeightSize = (MeasureSpec.getSize(parentHeightMeasureSpec)
                    - parentInsets.systemWindowInsetTop - parentInsets.systemWindowInsetBottom)
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
