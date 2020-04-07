/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings

import android.content.Context
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.takisoft.preferencex.EditTextPreference
import androidx.preference.EditTextPreference as AndroidXEditTextPreference

class PasswordPreference : EditTextPreference {
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
        if (summaryProvider is AndroidXEditTextPreference.SimpleSummaryProvider) {
            summaryProvider = SimpleSummaryProvider
        }
    }

    object SimpleSummaryProvider : SummaryProvider<EditTextPreference> {
        override fun provideSummary(preference: EditTextPreference): CharSequence? {
            val text = preference.text
            return if (!text.isNullOrEmpty()) {
                PasswordTransformationMethod.getInstance().getTransformation(text, null)
            } else {
                AndroidXEditTextPreference.SimpleSummaryProvider.getInstance().provideSummary(
                    preference
                )
            }
        }
    }
}
