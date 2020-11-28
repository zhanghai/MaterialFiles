/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.theme.typeface

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import me.zhanghai.android.files.compat.nativeInstanceCompat

class TypefaceLayoutInflaterFactory(private val familyName: String) : LayoutInflater.Factory2 {
    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? =
        onCreateView(null, name, context, attrs)

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        val view = MaterialComponentsLayoutInflaterFactory.onCreateView(
            parent, name, context, attrs
        )
        if (view is TextView) {
            val typeface = view.typeface
            if (typeface.isDefaultOrSansSerif) {
                view.setTypeface(typeface.toFamily(familyName), typeface.style)
            }
        }
        if (view is Button) {
            view.isAllCaps = false
        }
        return view
    }

    private val Typeface.isDefaultOrSansSerif: Boolean
        get() =
            when (nativeInstanceCompat) {
                Typeface.DEFAULT.nativeInstanceCompat, Typeface.SANS_SERIF.nativeInstanceCompat ->
                    true
                else -> false
            }

    @SuppressLint("Range")
    private fun Typeface.toFamily(familyName: String): Typeface {
        var googleSansTypeface = Typeface.create(familyName, style)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (googleSansTypeface.weight != weight) {
                googleSansTypeface = Typeface.create(googleSansTypeface, weight, isItalic)
            }
        }
        return googleSansTypeface
    }
}
