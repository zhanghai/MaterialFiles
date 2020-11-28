/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.theme.typeface

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatViewInflaterCompat
import androidx.appcompat.widget.VectorEnabledTintResources
import com.google.android.material.theme.MaterialComponentsViewInflater

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
object MaterialComponentsLayoutInflaterFactory : LayoutInflater.Factory2 {
    private val viewInflater = MaterialComponentsViewInflater()

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? =
        onCreateView(null, name, context, attrs)

    /**
     * @see androidx.appcompat.app.AppCompatDelegateImpl.createView
     */
    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? =
        AppCompatViewInflaterCompat.createView(viewInflater, parent, name, context, attrs, false,
            false, true, VectorEnabledTintResources.shouldBeUsed())
}
