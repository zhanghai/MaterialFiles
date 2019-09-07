/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;
import me.zhanghai.android.files.settings.Settings;

public class AlertDialogBuilderCompat {

    private AlertDialogBuilderCompat() {}

    @NonNull
    public static AlertDialog.Builder create(@NonNull Context context,
                                             @StyleRes int overrideThemeResId) {
        if (Settings.MATERIAL_DESIGN_2.getValue()) {
            return new MaterialAlertDialogBuilder(context, overrideThemeResId);
        } else {
            // MaterialAlertDialogBuilder currently isn't completely the same as AlertDialog.Builder
            // when used as AppCompat.
            return new AlertDialog.Builder(context, overrideThemeResId);
        }
    }
}
