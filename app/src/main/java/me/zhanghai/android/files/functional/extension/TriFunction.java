/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.functional.extension;

import java.util.Objects;

import me.zhanghai.android.files.functional.compat.Function;

@FunctionalInterface
public interface TriFunction<T, U, V, R> {

    R apply(T t, U u, V v);

    default <W> TriFunction<T, U, V, W> andThen(Function<? super R, ? extends W> after) {
        Objects.requireNonNull(after);
        return (T t, U u, V v) -> after.apply(apply(t, u, v));
    }
}
