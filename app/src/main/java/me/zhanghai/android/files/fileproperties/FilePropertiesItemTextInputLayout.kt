/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.AttrRes
import androidx.core.view.updateLayoutParams
import com.google.android.material.textfield.TextInputLayout
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.getDrawableCompat
import me.zhanghai.android.files.compat.obtainStyledAttributesCompat
import me.zhanghai.android.files.compat.setTextAppearanceCompat
import me.zhanghai.android.files.compat.use
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.dpToDimensionPixelSize
import me.zhanghai.android.files.util.getColorStateListByAttr
import me.zhanghai.android.files.util.getResourceIdByAttr
import me.zhanghai.android.files.util.valueCompat

class FilePropertiesItemTextInputLayout : TextInputLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    init {
        isHintAnimationEnabled = false
        if (!Settings.MATERIAL_DESIGN_2.valueCompat) {
            defaultHintTextColor =
                context.getColorStateListByAttr(android.R.attr.textColorSecondary)
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child is EditText) {
            if (!Settings.MATERIAL_DESIGN_2.valueCompat) {
                child.setTextAppearanceCompat(R.style.TextAppearance_AppCompat_Subhead)
            }
        }
        super.addView(child, index, params)
        if (child is EditText) {
            setDropDown(!child.isTextSelectable)
        }
    }

    fun setDropDown(dropDown: Boolean) {
        val editText = editText!!
        val context = context
        if (Settings.MATERIAL_DESIGN_2.valueCompat) {
            if (dropDown) {
                endIconMode = END_ICON_CUSTOM
                endIconDrawable = context.getDrawableCompat(R.drawable.mtrl_ic_arrow_drop_down)
            } else {
                endIconMode = END_ICON_NONE
            }
        } else {
            @SuppressLint("RestrictedApi")
            editText.background = if (dropDown) {
                val spinnerStyleRes = context.getResourceIdByAttr(R.attr.spinnerStyle)
                // TODO: Use defStyleAttr = R.attr.spinnerStyle instead?
                context.obtainStyledAttributesCompat(
                    attrs = intArrayOf(android.R.attr.background), defStyleRes = spinnerStyleRes
                ).use { it.getDrawable(0) }
            } else {
                null
            }
            editText.updateLayoutParams<MarginLayoutParams> {
                marginEnd = if (dropDown) context.dpToDimensionPixelSize(-19) else 0
            }
            val verticalPadding = context.dpToDimensionPixelSize(8)
            editText.setPadding(0, verticalPadding, 0, verticalPadding)
        }
    }
}
