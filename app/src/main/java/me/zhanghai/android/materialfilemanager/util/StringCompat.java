/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.text.TextUtils;

public class StringCompat {

    private StringCompat() {}

    public static String join(CharSequence delimiter, CharSequence... elements) {
        return TextUtils.join(delimiter, elements);
    }

    public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
        return TextUtils.join(delimiter, elements);
    }
}
