/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.colorpicker

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.preference.DialogPreference
import androidx.preference.PreferenceViewHolder
import me.zhanghai.android.files.R
import me.zhanghai.android.files.util.getFloatByAttr
import kotlin.math.roundToInt

abstract class BaseColorPreference : DialogPreference {
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
        widgetLayoutResource = R.layout.color_preference_widget
        dialogLayoutResource = R.layout.color_picker_dialog
        setPositiveButtonText(android.R.string.ok)
        setNegativeButtonText(android.R.string.cancel)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val swatchView = holder.findViewById(R.id.swatch)
        if (swatchView != null) {
            val swatchDrawable = swatchView.background as GradientDrawable
            swatchDrawable.setColor(value)
            var alpha = 0xFF
            if (!isEnabled) {
                val disabledAlpha = context.getFloatByAttr(android.R.attr.disabledAlpha)
                alpha = (disabledAlpha * alpha).roundToInt()
            }
            swatchDrawable.alpha = alpha
        }
    }

    @get:ColorInt
    abstract var value: Int

    @get:ColorInt
    abstract val defaultValue: Int

    abstract val entryValues: IntArray
}
