/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.MaterialShapeUtils

class SyncAppBarElevationToolbar : MaterialToolbar {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        var appBarLayout: AppBarLayout? = null
        var parent = parent
        while (parent != null) {
            if (parent is AppBarLayout) {
                appBarLayout = parent
                break
            }
            parent = parent.parent
        }
        if (appBarLayout != null) {
            var appBarLayoutBackground = appBarLayout.background
            if (appBarLayoutBackground !is SetElevationCallbackDrawable
                && appBarLayoutBackground is MaterialShapeDrawable) {
                appBarLayoutBackground = SetElevationCallbackDrawable(
                    appBarLayoutBackground, context
                ) { MaterialShapeUtils.setElevation(this, it) }
                appBarLayout.background = appBarLayoutBackground
            }
        }
    }

    private class SetElevationCallbackDrawable(
        drawable: MaterialShapeDrawable,
        context: Context,
        private val callback: (Float) -> Unit
    ) : MaterialShapeDrawable() {
        init {
            fillColor = drawable.fillColor
            initializeElevationOverlay(context)
        }

        override fun setElevation(elevation: Float) {
            super.setElevation(elevation)

            callback(elevation)
        }
    }
}
