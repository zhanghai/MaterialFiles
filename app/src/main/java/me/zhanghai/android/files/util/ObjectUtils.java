/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ObjectUtils {

    private ObjectUtils() {}

    public static final Object EMPTY_OBJECT = new Object();

    /**
     * @deprecated Should never be used.
     */
    @Nullable
    public static <T> T firstNonNull(@Nullable T first) {
        return first;
    }

    @Nullable
    public static <T> T firstNonNull(@Nullable T first, @Nullable T second) {
        return first != null ? first : second;
    }

    @Nullable
    public static <T> T firstNonNull(@Nullable T first, @Nullable T second, @Nullable T third) {
        return first != null ? first
                : second != null ? second
                : third;
    }

    /**
     * @deprecated Do you really need this?
     */
    @Nullable
    @SafeVarargs
    public static <T> T firstNonNull(@NonNull T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Nullable
    public static String toStringOrNull(@Nullable Object object) {
        return object != null ? object.toString() : null;
    }
}
