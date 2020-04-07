/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.AttrRes
import androidx.core.view.updatePaddingRelative
import com.google.android.material.textfield.TextInputLayout
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.obtainStyledAttributesCompat
import me.zhanghai.android.files.compat.setTextAppearanceCompat
import me.zhanghai.android.files.compat.use
import me.zhanghai.android.files.util.dpToDimensionPixelSize
import me.zhanghai.android.files.util.getColorStateListByAttr
import me.zhanghai.android.files.util.getResourceIdByAttr

class AppCompatTextInputLayout : TextInputLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    init {
        isHintAnimationEnabled = false
        defaultHintTextColor = context.getColorStateListByAttr(android.R.attr.textColorSecondary)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child is EditText) {
            val context = context
            val verticalPadding = context.dpToDimensionPixelSize(8)
            if (child.isTextSelectable) {
                child.background = null
                child.setPadding(0, verticalPadding, 0, verticalPadding)
            } else {
                val spinnerStyleRes = context.getResourceIdByAttr(R.attr.spinnerStyle)
                // TODO: Use defStyleAttr = R.attr.spinnerStyle instead?
                @SuppressLint("RestrictedApi")
                child.background = context.obtainStyledAttributesCompat(
                    attrs = intArrayOf(android.R.attr.background), defStyleRes = spinnerStyleRes
                ).use { it.getDrawable(0) }
                (params as MarginLayoutParams).marginEnd = context.dpToDimensionPixelSize(-19)
                child.updatePaddingRelative(top = verticalPadding, bottom = verticalPadding)
            }
            child.setTextAppearanceCompat(R.style.TextAppearance_AppCompat_Subhead)
        }
        super.addView(child, index, params)
    }
}
