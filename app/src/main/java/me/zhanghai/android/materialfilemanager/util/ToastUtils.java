/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

    private ToastUtils() {}

    public static void show(CharSequence text, int duration, Context context) {
        Toast.makeText(context, text, duration).show();
    }
    public static void show(int resId, int duration, Context context) {
        show(context.getText(resId), duration, context);
    }

    public static void show(CharSequence text, Context context) {
        show(text, Toast.LENGTH_SHORT, context);
    }

    public static void show(int resId, Context context) {
        show(context.getText(resId), context);
    }

    public static void showLong(CharSequence text, Context context) {
        show(text, Toast.LENGTH_LONG, context);
    }

    public static void showLong(int resId, Context context) {
        showLong(context.getText(resId), context);
    }
}
