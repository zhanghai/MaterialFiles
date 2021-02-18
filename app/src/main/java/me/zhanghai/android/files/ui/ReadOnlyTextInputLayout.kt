/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.AttrRes
import com.google.android.material.textfield.TextInputLayout
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.getDrawableCompat

class ReadOnlyTextInputLayout : TextInputLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    init {
        isExpandedHintEnabled = false
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.addView(child, index, params)

        if (child is EditText) {
            setDropDown(!child.isTextSelectable)
        }
    }

    fun setDropDown(dropDown: Boolean) {
        if (dropDown) {
            endIconMode = END_ICON_CUSTOM
            endIconDrawable = context.getDrawableCompat(R.drawable.mtrl_ic_arrow_drop_down)
        } else {
            endIconMode = END_ICON_NONE
        }
    }
}
