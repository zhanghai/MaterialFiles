/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import androidx.core.graphics.ColorUtils
import com.google.android.material.shape.MaterialShapeDrawable
import me.zhanghai.android.files.util.activity

class CoordinatorAppBarLayout : FitsSystemWindowsAppBarLayout {
    private val syncBackgroundColorViews = mutableListOf<View>()

    private var offset = 0
    private val tempClipBounds = Rect()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    init {
        val defaultBackgroundColor = (background as? MaterialShapeDrawable)?.fillColor?.defaultColor
        if (defaultBackgroundColor != null) {
            val window = context.activity!!.window
            val statusBarColor = window.statusBarColor
            if (defaultBackgroundColor == statusBarColor
                || defaultBackgroundColor == ColorUtils.setAlphaComponent(statusBarColor, 0xFF)) {
                window.statusBarColor = Color.TRANSPARENT
            }
        }

        addLiftOnScrollListener { _, backgroundColor ->
            onBackgroundColorChanged(backgroundColor)
        }

        addOnOffsetChangedListener { _, offset ->
            this.offset = offset
            updateFirstChildClipBounds()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        getChildAt(0)?.let {
            it.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                updateFirstChildClipBounds()
            }
        }
    }

    fun syncBackgroundColorTo(view: View) {
        syncBackgroundColorViews += view
    }

    private fun onBackgroundColorChanged(backgroundColor: Int) {
        syncBackgroundColorViews.forEach {
            (it.background as? MaterialShapeDrawable)?.fillColor =
                ColorStateList.valueOf(backgroundColor)
        }
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
}
