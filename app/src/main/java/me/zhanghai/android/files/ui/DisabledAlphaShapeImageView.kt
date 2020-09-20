/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.imageview.ShapeableImageView
import me.zhanghai.android.files.util.getFloatByAttr
import kotlin.math.roundToInt

class DisabledAlphaShapeImageView : ShapeableImageView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)

        updateImageAlpha()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()

        updateImageAlpha()
    }

    private fun updateImageAlpha() {
        var alpha = 0xFF
        val drawable = drawable
        // AdaptiveIconDrawable might be stateful without respecting enabled state.
        val isAdaptiveIconDrawable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && drawable is AdaptiveIconDrawable
        if (drawable == null || isAdaptiveIconDrawable || !drawable.isStateful) {
            val enabled = android.R.attr.state_enabled in drawableState
            if (!enabled) {
                val disabledAlpha = context.getFloatByAttr(android.R.attr.disabledAlpha)
                alpha = (disabledAlpha * alpha).roundToInt()
            }
        }
        imageAlpha = alpha
    }
}
