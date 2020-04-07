/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.text.Editable
import android.text.TextWatcher

interface SimpleTextWatcher : TextWatcher {
    override fun afterTextChanged(text: Editable) {}

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {}
}
