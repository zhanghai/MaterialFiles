/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.content.Context
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.valueCompat

object AlertDialogBuilderCompat {
    fun create(context: Context, @StyleRes overrideThemeResId: Int): AlertDialog.Builder =
        if (Settings.MATERIAL_DESIGN_2.valueCompat) {
            MaterialAlertDialogBuilder(context, overrideThemeResId)
        } else {
            // MaterialAlertDialogBuilder currently isn't completely the same as AlertDialog.Builder
            // when used as AppCompat.
            AlertDialog.Builder(context, overrideThemeResId)
        }
}
