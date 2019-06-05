/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.functional.throwing;

import java9.util.Objects;
import me.zhanghai.android.files.functional.FunctionalException;
import me.zhanghai.android.files.functional.extension.QuadFunction;

@FunctionalInterface
public interface ThrowingQuadFunction<T, U, V, W, R> extends QuadFunction<T, U, V, W, R> {

    R applyThrows(T t, U u, V v, W w) throws Exception;

    default R apply(T t, U u, V v, W w) {
        try {
            return applyThrows(t, u, v, w);
        } catch (Exception e) {
            throw new FunctionalException(e);
        }
    }

    default <X> ThrowingQuadFunction<T, U, V, W, X> andThen(ThrowingFunction<? super R, ? extends X> after) {
        Objects.requireNonNull(after);
        return (T t, U u, V v, W w) -> after.applyThrows(applyThrows(t, u, v, w));
    }
}
