/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.takisoft.preferencex.EditTextPreference

class DefaultIfEmptyEditTextPreference : EditTextPreference {
    private var defaultValue: String? = null

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

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? =
        super.onGetDefaultValue(a, index).also { defaultValue = it as String? }

    override fun setDefaultValue(defaultValue: Any?) {
        super.setDefaultValue(defaultValue)

        this.defaultValue = defaultValue as String?
    }

    override fun setText(text: String?) {
        val text = if (!text.isNullOrEmpty()) text else defaultValue
        super.setText(text)
    }
}
