/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.icu.text.ListFormatter;
import android.os.Build;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import me.zhanghai.java.functional.Functional;

public class ListFormatterCompat {

    private ListFormatterCompat() {}

    @NonNull
    public static String format(Object... items) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return ListFormatter.getInstance().format(items);
        } else {
            return formatCompat(Arrays.asList(items));
        }
    }

    /**
     * @see ListFormatter#format(Collection)
     */
    @NonNull
    public static String format(Collection<?> items) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return ListFormatter.getInstance().format(items);
        } else {
            return formatCompat(items);
        }
    }

    @NonNull
    private static String formatCompat(Collection<?> items) {
        List<String> strings = Functional.map(items, Object::toString);
        return StringCompat.join(", ", strings);
    }
}
