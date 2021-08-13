/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.ColorUtils
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.MaterialShapeDrawableAccessor
import com.google.android.material.shape.MaterialShapeUtils
import me.zhanghai.android.files.util.activity

class CoordinatorAppBarLayout : AppBarLayout {
    private val tempConsumed = IntArray(2)

    private var offset = 0
    private val tempClipBounds = Rect()

    private val syncBackgroundElevationViews = mutableListOf<View>()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    init {
        fitsSystemWindows = true

        val background = background
        val backgroundColor = (background as? MaterialShapeDrawable)?.fillColor?.defaultColor
        if (backgroundColor != null) {
            val window = context.activity!!.window
            val statusBarColor = window.statusBarColor
            if (backgroundColor == statusBarColor
                || backgroundColor == ColorUtils.setAlphaComponent(statusBarColor, 0xFF)) {
                window.statusBarColor = Color.TRANSPARENT
            }
        }

        viewTreeObserver.addOnPreDrawListener {
            updateLiftedState()
            true
        }

        maybeUseMd3AppBarElevationOverlay()

        if (background is MaterialShapeDrawable) {
            this.background = OnElevationChangedMaterialShapeDrawable(
                background, this::onBackgroundElevationChanged
            )
        }

        addOnOffsetChangedListener(OnOffsetChangedListener { _, offset ->
            this.offset = offset
            updateFirstChildClipBounds()
        })
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        getChildAt(0)?.let {
            it.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                updateFirstChildClipBounds()
            }
        }
    }

    fun syncBackgroundElevationTo(view: View) {
        syncBackgroundElevationViews += view
    }

    private fun onBackgroundElevationChanged(elevation: Float) {
        syncBackgroundElevationViews.forEach { MaterialShapeUtils.setElevation(it, elevation) }
    }

    private fun updateLiftedState() {
        if (!isLiftOnScroll) {
            return
        }
        val coordinatorLayout = parent as? CoordinatorLayout ?: return
        // Call AppBarLayout.Behavior.onNestedPreScroll() with dy == 0 to update lifted state.
        val behavior = (layoutParams as CoordinatorLayout.LayoutParams).behavior ?: return
        behavior.onNestedPreScroll(
            coordinatorLayout, this, coordinatorLayout, 0, 0, tempConsumed, 0
        )
    }

    private fun updateFirstChildClipBounds() {
        val firstChild = getChildAt(0) ?: return
        tempClipBounds.set(0, -offset, firstChild.width, firstChild.height)
        // Work around a bug before Android N that an empty clip bounds doesn't clip.
        // Making the clip bounds somewhere outside view bounds doesn't work, so as a hack we just
        // assume that the first child won't draw anything in its top-left pixel.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            if (tempClipBounds.isEmpty) {
                tempClipBounds.set(0, 0, 1, 1)
            }
        }
        firstChild.clipBounds = tempClipBounds
    }

    private class OnElevationChangedMaterialShapeDrawable(
        drawable: MaterialShapeDrawable,
        private val onElevationChanged: (Float) -> Unit
    ) : MaterialShapeDrawable() {
        init {
            fillColor = drawable.fillColor
            MaterialShapeDrawableAccessor.setElevationOverlayProvider(
                this, MaterialShapeDrawableAccessor.getElevationOverlayProvider(drawable)
            )
            MaterialShapeDrawableAccessor.updateZ(this)
        }

        override fun setElevation(elevation: Float) {
            super.setElevation(elevation)

            onElevationChanged(elevation)
        }
    }
}
