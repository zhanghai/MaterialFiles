/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.functional.throwing;

import java9.util.Objects;
import me.zhanghai.android.files.functional.FunctionalException;
import me.zhanghai.android.files.functional.extension.TriFunction;

@FunctionalInterface
public interface ThrowingTriFunction<T, U, V, R> extends TriFunction<T, U, V, R> {

    R applyThrows(T t, U u, V v) throws Exception;

    default R apply(T t, U u, V v) {
        try {
            return applyThrows(t, u, v);
        } catch (Exception e) {
            throw new FunctionalException(e);
        }
    }

    default <W> ThrowingTriFunction<T, U, V, W> andThen(ThrowingFunction<? super R, ? extends W> after) {
        Objects.requireNonNull(after);
        return (T t, U u, V v) -> after.applyThrows(applyThrows(t, u, v));
    }
}
