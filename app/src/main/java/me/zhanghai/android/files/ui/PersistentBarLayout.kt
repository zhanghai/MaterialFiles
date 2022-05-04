/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.core.content.res.use
import androidx.core.view.children
import androidx.core.view.isInvisible
import androidx.customview.widget.ViewDragHelper
import me.zhanghai.android.files.util.layoutInStatusBar

/**
 * @see PersistentDrawerLayout
 */
class PersistentBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {
    private val topDragger = ViewDragHelper.create(this, ViewDragCallback(Gravity.TOP))
    private val bottomDragger = ViewDragHelper.create(this, ViewDragCallback(Gravity.BOTTOM))

    private var lastInsets: WindowInsets? = null

    init {
        if (fitsSystemWindows) {
            layoutInStatusBar = true
        }
    }

    override fun dispatchApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (!fitsSystemWindows) {
            return insets
        }
        for (child in children) {
            if (isBarView(child)) {
                if (isTopBarView(child)) {
                    child.dispatchApplyWindowInsets(
                        insets.replaceSystemWindowInsets(
                            insets.systemWindowInsetLeft, insets.systemWindowInsetTop,
                            insets.systemWindowInsetRight, 0
                        )
                    )
                } else {
                    child.dispatchApplyWindowInsets(
                        insets.replaceSystemWindowInsets(
                            insets.systemWindowInsetLeft, 0, insets.systemWindowInsetRight,
                            insets.systemWindowInsetBottom
                        )
                    )
                }
            } else if (isFillView(child)) {
                child.dispatchApplyWindowInsets(insets)
            }
        }
        lastInsets = insets
        updateContentViewsWindowInsets()
        return insets.consumeSystemWindowInsets()
    }

    private fun updateContentViewsWindowInsets() {
        var contentInsets = lastInsets ?: return
        for (child in children) {
            if (isBarView(child)) {
                val childLayoutParams = child.layoutParams as LayoutParams
                val childRange = (childLayoutParams.topMargin + child.measuredHeight
                    + childLayoutParams.bottomMargin)
                val childConsumedInset = (childRange * childLayoutParams.offset).toInt()
                contentInsets = if (isTopBarView(child)) {
                    contentInsets.replaceSystemWindowInsets(
                        contentInsets.systemWindowInsetLeft,
                        (contentInsets.systemWindowInsetTop - childConsumedInset).coerceAtLeast(0),
                        contentInsets.systemWindowInsetRight,
                        contentInsets.systemWindowInsetBottom
                    )
                } else {
                    contentInsets.replaceSystemWindowInsets(
                        contentInsets.systemWindowInsetLeft,
                        contentInsets.systemWindowInsetTop,
                        contentInsets.systemWindowInsetRight,
                        (contentInsets.systemWindowInsetBottom - childConsumedInset)
                            .coerceAtLeast(0)
                    )
                }
            }
        }
        for (child in children) {
            if (isContentView(child)) {
                child.dispatchApplyWindowInsets(contentInsets)
            }
        }
    }

    override fun computeScroll() {
        val topSettling = topDragger.continueSettling(true)
        val bottomSettling = bottomDragger.continueSettling(true)
        if (topSettling || bottomSettling) {
            postInvalidateOnAnimation()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            if (isInEditMode) {
                if (widthMode == MeasureSpec.UNSPECIFIED) {
                    widthSize = 300
                }
                if (heightMode == MeasureSpec.UNSPECIFIED) {
                    heightSize = 300
                }
            } else {
                throw IllegalArgumentException(
                    "BarLayout must be measured with MeasureSpec.EXACTLY"
                )
            }
        }
        setMeasuredDimension(widthSize, heightSize)
        var hasTopBar = false
        var hasBottomBar = false
        for (child in children) {
            if (child.visibility == View.GONE) {
                continue
            }
            val isBar = isBarView(child)
            if (isBar || isFillView(child)) {
                if (isBar) {
                    val isTopBar = isTopBarView(child)
                    check(!((isTopBar && hasTopBar) || (!isTopBar && hasBottomBar))) {
                        ("Child $child is a second ${if (isTopBar) "top" else "bottom"} bar")
                    }
                    if (isTopBar) {
                        hasTopBar = true
                    } else {
                        hasBottomBar = true
                    }
                }
                val childLayoutParams = child.layoutParams as LayoutParams
                val childWidthSpec = getChildMeasureSpec(
                    widthMeasureSpec, childLayoutParams.leftMargin + childLayoutParams.rightMargin,
                    childLayoutParams.width
                )
                val childHeightSpec = getChildMeasureSpec(
                    heightMeasureSpec, childLayoutParams.topMargin + childLayoutParams.bottomMargin,
                    childLayoutParams.height
                )
                child.measure(childWidthSpec, childHeightSpec)
            } else check(isContentView(child)) {
                ("Child $child does not have a valid layout_gravity - must be Gravity.LEFT,"
                    + " Gravity.RIGHT, Gravity.NO_GRAVITY or Gravity.FILL")
            }
        }
        updateContentViewsWindowInsets()
        measureContentViews()
    }

    private fun measureContentViews() {
        val contentWidth = measuredWidth
        var contentHeight = measuredHeight
        for (child in children) {
            if (child.visibility == View.GONE) {
                continue
            }
            if (isBarView(child)) {
                val childLayoutParams = child.layoutParams as LayoutParams
                val childRange = (childLayoutParams.topMargin + child.measuredHeight
                    + childLayoutParams.bottomMargin)
                contentHeight -= (childRange * childLayoutParams.offset).toInt()
            }
        }
        for (child in children) {
            if (child.visibility == View.GONE) {
                continue
            }
            if (isContentView(child)) {
                val childLayoutParams = child.layoutParams as LayoutParams
                val childWidthSpec = MeasureSpec.makeMeasureSpec(
                    contentWidth - childLayoutParams.leftMargin - childLayoutParams.rightMargin,
                    MeasureSpec.EXACTLY
                )
                val contentHeightSpec = MeasureSpec.makeMeasureSpec(
                    contentHeight - childLayoutParams.topMargin - childLayoutParams.bottomMargin,
                    MeasureSpec.EXACTLY
                )
                child.measure(childWidthSpec, contentHeightSpec)
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        for (child in children) {
            if (child.visibility == View.GONE) {
                continue
            }
            if (isBarView(child)) {
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                val childLayoutParams = child.layoutParams as LayoutParams
                val childTop = computeBarViewTop(child)
                val childHorizontalGravity = Gravity.getAbsoluteGravity(
                    childLayoutParams.gravity, layoutDirection
                ) and Gravity.HORIZONTAL_GRAVITY_MASK
                val width = right - left
                when (childHorizontalGravity) {
                    Gravity.LEFT -> child.layout(
                        childLayoutParams.leftMargin, childTop,
                        childLayoutParams.leftMargin + childWidth, childTop + childHeight
                    )
                    Gravity.RIGHT -> {
                        val childRight = width - childLayoutParams.rightMargin
                        child.layout(
                            childRight - childWidth, childTop, childRight, childTop + childHeight
                        )
                    }
                    Gravity.CENTER_HORIZONTAL -> {
                        val childLeft = ((width - childWidth) / 2 + childLayoutParams.leftMargin
                            - childLayoutParams.rightMargin)
                        child.layout(
                            childLeft, childTop, childLeft + childWidth, childTop + childHeight
                        )
                    }
                    else -> child.layout(
                        childLayoutParams.leftMargin, childTop,
                        childLayoutParams.leftMargin + childWidth, childTop + childHeight
                    )
                }
                child.isInvisible = childLayoutParams.offset <= 0
            } else if (isFillView(child)) {
                val childLayoutParams = child.layoutParams as LayoutParams
                child.layout(
                    childLayoutParams.leftMargin, childLayoutParams.topMargin,
                    childLayoutParams.leftMargin + child.measuredWidth,
                    childLayoutParams.topMargin + child.measuredHeight
                )
            }
        }
        layoutContentViews()
    }

    private fun computeBarViewTop(barView: View): Int {
        val childLayoutParams = barView.layoutParams as LayoutParams
        val childRange = (childLayoutParams.topMargin + barView.measuredHeight
            + childLayoutParams.bottomMargin)
        return if (isTopBarView(barView)) {
            (-childRange + (childRange * childLayoutParams.offset).toInt()
                + childLayoutParams.topMargin)
        } else {
            (measuredHeight - (childRange * childLayoutParams.offset).toInt()
                + childLayoutParams.bottomMargin)
        }
    }

    private fun layoutContentViews() {
        var contentTop = 0
        for (child in children) {
            if (child.visibility == View.GONE) {
                continue
            }
            if (isBarView(child)) {
                if (isTopBarView(child)) {
                    val childLayoutParams = child.layoutParams as LayoutParams
                    contentTop = child.bottom + childLayoutParams.bottomMargin
                }
            }
        }
        for (child in children) {
            if (child.visibility == View.GONE) {
                continue
            }
            if (isContentView(child)) {
                val childLayoutParams = child.layoutParams as LayoutParams
                val childTop = contentTop + childLayoutParams.topMargin
                child.layout(
                    childLayoutParams.leftMargin, childTop,
                    childLayoutParams.leftMargin + child.measuredWidth,
                    childTop + child.measuredHeight
                )
            }
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams =
        LayoutParams(context, attrs)

    override fun generateLayoutParams(
        layoutParams: ViewGroup.LayoutParams
    ): ViewGroup.LayoutParams =
        when (layoutParams) {
            is LayoutParams -> LayoutParams(layoutParams)
            is MarginLayoutParams -> LayoutParams(layoutParams)
            else -> LayoutParams(layoutParams)
        }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams =
        LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    override fun checkLayoutParams(layoutParams: ViewGroup.LayoutParams): Boolean =
        layoutParams is LayoutParams && super.checkLayoutParams(layoutParams)

    fun isBarShown(barView: View): Boolean {
        require(isBarView(barView)) { "View $barView is not a bar" }
        val childLayoutParams = barView.layoutParams as LayoutParams
        return childLayoutParams.isShown
    }

    fun isBarShown(gravity: Int): Boolean {
        val barView = findBarView(gravity)
            ?: throw IllegalArgumentException("No bar view found with gravity $gravity")
        return isBarShown(barView)
    }

    fun showBar(barView: View, animate: Boolean = true) {
        require(isBarView(barView)) { "View $barView is not a bar" }
        val childLayoutParams = barView.layoutParams as LayoutParams
        if (childLayoutParams.isShown && childLayoutParams.offset == 1f) {
            return
        }
        childLayoutParams.isShown = true
        if (!isLaidOut) {
            childLayoutParams.offset = 1f
        } else if (animate) {
            if (isTopBarView(barView)) {
                topDragger.smoothSlideViewTo(barView, barView.left, 0)
            } else {
                bottomDragger.smoothSlideViewTo(
                    barView, barView.left, height - barView.height - childLayoutParams.bottomMargin
                )
            }
        } else {
            moveBarToOffset(barView, 1f)
            updateContentViewsWindowInsets()
            measureContentViews()
            layoutContentViews()
        }
        invalidate()
    }

    fun showBar(gravity: Int, animate: Boolean = true) {
        val barView = findBarView(gravity)
            ?: throw IllegalArgumentException("No bar view found with gravity $gravity")
        showBar(barView, animate)
    }

    fun hideBar(barView: View, animate: Boolean = true) {
        require(isBarView(barView)) { "View $barView is not a bar" }
        val childLayoutParams = barView.layoutParams as LayoutParams
        if (!childLayoutParams.isShown && childLayoutParams.offset == 0f) {
            return
        }
        childLayoutParams.isShown = false
        if (!isLaidOut) {
            childLayoutParams.offset = 0f
        } else if (animate) {
            if (isTopBarView(barView)) {
                topDragger.smoothSlideViewTo(
                    barView, barView.left, -barView.height - childLayoutParams.bottomMargin
                )
            } else {
                bottomDragger.smoothSlideViewTo(barView, barView.left, height)
            }
        } else {
            childLayoutParams.offset = 0f
            moveBarToOffset(barView, 0f)
            updateContentViewsWindowInsets()
            measureContentViews()
            layoutContentViews()
        }
        invalidate()
    }

    fun hideBar(gravity: Int, animate: Boolean = true) {
        val barView = findBarView(gravity)
            ?: throw IllegalArgumentException("No bar view found with gravity $gravity")
        hideBar(barView, animate)
    }

    private fun moveBarToOffset(barView: View, offset: Float) {
        val childLayoutParams = barView.layoutParams as LayoutParams
        if (childLayoutParams.offset == offset) {
            return
        }
        childLayoutParams.offset = offset
        val oldChildTop = barView.top
        val newChildTop = computeBarViewTop(barView)
        barView.offsetTopAndBottom(newChildTop - oldChildTop)
        barView.isInvisible = offset <= 0
    }

    fun toggleBar(barView: View) {
        if (isBarShown(barView)) {
            hideBar(barView)
        } else {
            showBar(barView)
        }
    }

    fun toggleBar(gravity: Int) {
        val barView = findBarView(gravity)
            ?: throw IllegalArgumentException("No bar view found with gravity $gravity")
        toggleBar(barView)
    }

    private fun findBarView(gravity: Int): View? {
        val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK
        for (child in children) {
            val childVerticalGravity = getChildVerticalGravity(child)
            if (childVerticalGravity == verticalGravity) {
                return child
            }
        }
        return null
    }

    private fun isBarView(child: View): Boolean {
        val verticalGravity = getChildVerticalGravity(child)
        return verticalGravity == Gravity.TOP || verticalGravity == Gravity.BOTTOM
    }

    private fun isTopBarView(barView: View): Boolean {
        val verticalGravity = getChildVerticalGravity(barView)
        return verticalGravity == Gravity.TOP
    }

    private fun isContentView(child: View): Boolean {
        return getChildGravity(child) == Gravity.NO_GRAVITY
    }

    private fun isFillView(child: View): Boolean {
        return getChildGravity(child) == Gravity.FILL
    }

    private fun getChildGravity(child: View): Int {
        return (child.layoutParams as LayoutParams).gravity
    }

    private fun getChildVerticalGravity(child: View): Int {
        return getChildGravity(child) and Gravity.VERTICAL_GRAVITY_MASK
    }

    private inner class ViewDragCallback(private val gravity: Int) : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean = false

        override fun onViewPositionChanged(
            changedView: View, left: Int, top: Int, dx: Int, dy: Int
        ) {
            val childRange = getViewVerticalDragRange(changedView)
            val childLayoutParams = changedView.layoutParams as LayoutParams
            if (isTopBarView(changedView)) {
                childLayoutParams.offset = (top - childLayoutParams.topMargin + childRange)
                    .toFloat() / childRange
            } else {
                val height = height
                childLayoutParams.offset = ((childLayoutParams.topMargin + height - top).toFloat()
                    / childRange)
            }
            changedView.isInvisible = childLayoutParams.offset <= 0
            updateContentViewsWindowInsets()
            measureContentViews()
            layoutContentViews()
        }

        override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
            closeOtherBar()
        }

        private fun closeOtherBar() {
            val otherGravity = if (gravity == Gravity.TOP) Gravity.BOTTOM else Gravity.TOP
            val otherBar = findBarView(otherGravity)
            otherBar?.let { hideBar(it) }
        }

        override fun getViewVerticalDragRange(child: View): Int {
            if (!isBarView(child)) {
                return 0
            }
            val childLayoutParams = child.layoutParams as LayoutParams
            return childLayoutParams.topMargin + child.height + childLayoutParams.bottomMargin
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int = child.left

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int =
            if (isTopBarView(child)) {
                top.coerceIn(-getViewVerticalDragRange(child)..0)
            } else {
                val height = height
                top.coerceIn(height - getViewVerticalDragRange(child)..height)
            }
    }

    class LayoutParams : MarginLayoutParams {
        var gravity = Gravity.NO_GRAVITY
        var offset = 0f
        var isShown = false

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            gravity = context.obtainStyledAttributes(attrs, ATTRS)
                .use { it.getInt(0, Gravity.NO_GRAVITY) }
        }

        constructor(width: Int, height: Int) : super(width, height)

        constructor(width: Int, height: Int, gravity: Int) : this(width, height) {
            this.gravity = gravity
        }

        constructor(source: LayoutParams) : super(source) {
            gravity = source.gravity
        }

        constructor(source: MarginLayoutParams) : super(source)

        constructor(source: ViewGroup.LayoutParams) : super(source)

        companion object {
            private val ATTRS = intArrayOf(android.R.attr.layout_gravity)
        }
    }
}
