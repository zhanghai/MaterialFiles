/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.ColorUtils
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.MaterialShapeUtils
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.activity
import me.zhanghai.android.files.util.getColorByAttr

class CoordinatorAppBarLayout : AppBarLayout {
    private val tempConsumed = IntArray(2)

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

        val appBarSurfaceColor = context.getColorByAttr(R.attr.colorAppBarSurface)
        val primaryDarkColor = context.getColorByAttr(R.attr.colorPrimaryDark)
        if (primaryDarkColor == appBarSurfaceColor
            || ColorUtils.setAlphaComponent(primaryDarkColor, 255) == appBarSurfaceColor) {
            context.activity!!.window.statusBarColor = Color.TRANSPARENT
        }

        val background = background
        if (background is MaterialShapeDrawable) {
            this.background = OnElevationChangedMaterialShapeDrawable(
                background, context, this::onBackgroundElevationChanged
            )
        }
    }

    override fun draw(canvas: Canvas) {
        if (isLiftOnScroll) {
            val coordinatorLayout = parent as? CoordinatorLayout
            if (coordinatorLayout != null) {
                // Call AppBarLayout.Behavior.onNestedPreScroll() with dy == 0 to update lifted
                // state.
                val behavior = (layoutParams as CoordinatorLayout.LayoutParams).behavior!!
                behavior.onNestedPreScroll(
                    coordinatorLayout, this, coordinatorLayout, 0, 0, tempConsumed, 0
                )
            }
        }

        super.draw(canvas)
    }

    fun syncBackgroundElevationTo(view: View) {
        syncBackgroundElevationViews += view
    }

    private fun onBackgroundElevationChanged(elevation: Float) {
        syncBackgroundElevationViews.forEach { MaterialShapeUtils.setElevation(it, elevation) }
    }

    private class OnElevationChangedMaterialShapeDrawable(
        drawable: MaterialShapeDrawable,
        context: Context,
        private val onElevationChanged: (Float) -> Unit
    ) : MaterialShapeDrawable() {
        init {
            fillColor = drawable.fillColor
            initializeElevationOverlay(context)
        }

        override fun setElevation(elevation: Float) {
            super.setElevation(elevation)

            onElevationChanged(elevation)
        }
    }
}
