/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcelable
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.preference.EditTextPreference.OnBindEditTextListener
import com.takisoft.preferencex.EditTextPreference
import kotlinx.parcelize.Parcelize
import me.zhanghai.android.files.compat.DigitsKeyListenerCompat
import me.zhanghai.android.files.util.ParcelableState

class NonNegativeIntegerPreference : EditTextPreference {
    private var isIntegerSet = false
    var integer: Int = 0
        set(integer) {
            if (integer < 0) {
                return
            }
            val changed = field != integer
            if (changed || !isIntegerSet) {
                field = integer
                isIntegerSet = true
                persistInt(field)
                if (changed) {
                    notifyChanged()
                }
            }
        }

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
        onBindEditTextListener = OnBindEditTextListener {
            it.keyListener = DigitsKeyListenerCompat.getInstance(null, false, false)
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Int = a.getInteger(index, 0)

    override fun onSetInitialValue(defaultValue: Any?) {
        val defaultValueInt = if (defaultValue != null) defaultValue as Int else 0
        integer = getPersistedInt(defaultValueInt)
    }

    override fun setText(text: String?) {
        text ?: return
        integer = try {
            text.toInt()
        } catch (e: NumberFormatException) {
            return
        }
    }

    override fun getText(): String = integer.toString()

    override fun shouldDisableDependents(): Boolean = !isEnabled

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            return superState
        }

        return State(superState, integer)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state !is State) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        integer = state.integer
    }

    @Parcelize
    private class State(val superState: Parcelable?, val integer: Int) : ParcelableState
}
