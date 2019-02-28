/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.functional.extension;

import java.util.Objects;

@FunctionalInterface
public interface QuadConsumer<T, U, V, W> {

    void accept(T t, U u, V v, W w);

    default QuadConsumer<T, U, V, W> andThen(
            QuadConsumer<? super T, ? super U, ? super V, ? super W> after) {
        Objects.requireNonNull(after);
        return (t, u, v, w) -> {
            accept(t, u, v, w);
            after.accept(t, u, v, w);
        };
    }
}
