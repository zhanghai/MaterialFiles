/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.functional;

import java.util.Objects;

import me.zhanghai.android.materialfilemanager.functional.compat.Consumer;

public class IterableCompat {

    private IterableCompat() {}

    public static <T> void forEach(Iterable<T> iterable, Consumer<T> action) {
        Objects.requireNonNull(iterable);
        for (T t : iterable) {
            action.accept(t);
        }
    }
}
