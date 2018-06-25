/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.functional.throwing;

import java.util.Objects;

import me.zhanghai.android.materialfilemanager.functional.FunctionalException;
import me.zhanghai.android.materialfilemanager.functional.extension.TriPredicate;

@FunctionalInterface
public interface ThrowingTriPredicate<T, U, V> extends TriPredicate<T, U, V> {

    boolean testThrows(T t, U u, V v) throws Exception;

    default boolean test(T t, U u, V v) {
        try {
            return testThrows(t, u, v);
        } catch (Exception e) {
            throw new FunctionalException(e);
        }
    }

    default ThrowingTriPredicate<T, U, V> and(ThrowingTriPredicate<? super T, ? super U, ? super V> other) {
        Objects.requireNonNull(other);
        return (T t, U u, V v) -> testThrows(t, u, v) && other.testThrows(t, u, v);
    }

    default ThrowingTriPredicate<T, U, V> negate() {
        return (T t, U u, V v) -> !testThrows(t, u, v);
    }

    default ThrowingTriPredicate<T, U, V> or(ThrowingTriPredicate<? super T, ? super U, ? super V> other) {
        Objects.requireNonNull(other);
        return (T t, U u, V v) -> testThrows(t, u, v) || other.testThrows(t, u, v);
    }
}
