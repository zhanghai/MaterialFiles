/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.functional;

import java.util.Objects;

import me.zhanghai.android.files.functional.compat.Consumer;

public class IterableCompat {

    private IterableCompat() {}

    public static <T> void forEach(Iterable<T> iterable, Consumer<T> action) {
        Objects.requireNonNull(iterable);
        for (T t : iterable) {
            action.accept(t);
        }
    }
}
