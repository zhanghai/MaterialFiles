/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.theme.typeface

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.theme.MaterialComponentsViewInflater

object GoogleSansViewInflater : MaterialComponentsViewInflater() {
    override fun createButton(context: Context, attrs: AttributeSet): AppCompatButton =
        super.createButton(context, attrs).also {
            if (GoogleSansHelper.isActive) {
                it.isAllCaps = false
            }
        }
}
