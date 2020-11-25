/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.theme.custom

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import com.takisoft.preferencex.PreferenceFragmentCompat
import me.zhanghai.android.files.colorpicker.BaseColorPreference
import me.zhanghai.android.files.colorpicker.ColorPreferenceDialogFragment
import me.zhanghai.android.files.compat.getColorCompat

class ThemeColorPreference : BaseColorPreference {
    private lateinit var _stringValue: String
    var stringValue: String
        get() = _stringValue
        set(value) {
            _stringValue = value
            persistString(value)
            notifyChanged()
        }

    // We can't use lateinit for Int.
    private var initialValue: Int? = null
    override var value: Int
        // Deliberately only bind for the initial value, because we are going to restart the
        // activity upon change and we want to let the activity animation have the correct visual
        // appearance.
        @ColorInt
        get() {
            var initialValue = initialValue
            if (initialValue == null) {
                initialValue = entryValues[stringValue.toInt()]
                this.initialValue = initialValue
            }
            return initialValue
        }
        set(value) {
            stringValue = entryValues.indexOf(value).toString()
        }

    private lateinit var defaultStringValue: String
    override val defaultValue: Int
        @ColorInt
        get() = entryValues[defaultStringValue.toInt()]

    override var entryValues: IntArray
        private set

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
        val context = context
        entryValues = ThemeColor.values().map { context.getColorCompat(it.resourceId) }
            .toIntArray()
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? =
        a.getString(index).also { defaultStringValue = it!! }

    override fun onSetInitialValue(defaultValue: Any?) {
        stringValue = getPersistedString(defaultValue as String?)
    }

    companion object {
        init {
            PreferenceFragmentCompat.registerPreferenceFragment(
                ThemeColorPreference::class.java, ColorPreferenceDialogFragment::class.java
            )
        }
    }
}
