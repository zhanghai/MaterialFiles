/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import android.os.Build;

import androidx.annotation.NonNull;

public class StringCompat {

    private StringCompat() {}

    @NonNull
    public static String join(@NonNull CharSequence delimiter, @NonNull CharSequence... elements) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return String.join(delimiter, elements);
        } else {
            // TextUtils.join() can throw NullPointerException, so we cannot use it.
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (CharSequence element : elements) {
                if (first) {
                    first = false;
                } else {
                    builder.append(delimiter);
                }
                builder.append(element);
            }
            return builder.toString();
        }
    }

    @NonNull
    public static String join(@NonNull CharSequence delimiter,
                              @NonNull Iterable<? extends CharSequence> elements) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return String.join(delimiter, elements);
        } else {
            // TextUtils.join() can throw NullPointerException, so we cannot use it.
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (CharSequence element : elements) {
                if (first) {
                    first = false;
                } else {
                    builder.append(delimiter);
                }
                builder.append(element);
            }
            return builder.toString();
        }
    }
}
