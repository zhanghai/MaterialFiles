/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Checkable
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes

open class CheckableView : View, Checkable {
    private var _isChecked = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            refreshDrawableState()
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

    override fun isChecked(): Boolean = _isChecked

    override fun setChecked(checked: Boolean) {
        _isChecked = checked
    }

    override fun toggle() {
        _isChecked = !_isChecked
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray =
        super.onCreateDrawableState(extraSpace + 1).apply {
            if (_isChecked) {
                View.mergeDrawableStates(this, CHECKED_STATE_SET)
            }
        }

    companion object {
        private val CHECKED_STATE_SET = intArrayOf(android.R.attr.state_checked)
    }
}
