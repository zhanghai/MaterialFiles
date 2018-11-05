/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class ToastUtils {

    private ToastUtils() {}

    public static void show(@NonNull CharSequence text, int duration, @NonNull Context context) {
        Toast.makeText(context, text, duration).show();
    }
    public static void show(@StringRes int textRes, int duration, @NonNull Context context) {
        show(context.getText(textRes), duration, context);
    }

    public static void show(@NonNull CharSequence text, @NonNull Context context) {
        show(text, Toast.LENGTH_SHORT, context);
    }

    public static void show(@StringRes int textRes, @NonNull Context context) {
        show(context.getText(textRes), context);
    }

    public static void showLong(@NonNull CharSequence text, @NonNull Context context) {
        show(text, Toast.LENGTH_LONG, context);
    }

    public static void showLong(@StringRes int textRes, @NonNull Context context) {
        showLong(context.getText(textRes), context);
    }
}
