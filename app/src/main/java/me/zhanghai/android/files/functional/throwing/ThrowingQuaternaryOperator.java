/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.functional.throwing;

import java.util.Comparator;

import java9.util.Objects;

@FunctionalInterface
public interface ThrowingQuaternaryOperator<T> extends ThrowingQuadFunction<T, T, T, T, T> {

    static <T> ThrowingQuaternaryOperator<T> minBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b, c, d) -> comparator.compare(a, b) <= 0 ?
                comparator.compare(a, c) <= 0 ?
                        comparator.compare(a, d) <= 0 ? a : d
                        : comparator.compare(c, d) <= 0 ? c : d
                : comparator.compare(b, c) <= 0 ?
                        comparator.compare(b, d) <= 0 ? b : d
                        : comparator.compare(c, d) <= 0 ? c : d;
    }

    static <T> ThrowingQuaternaryOperator<T> maxBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b, c, d) -> comparator.compare(a, b) >= 0 ?
                comparator.compare(a, c) >= 0 ?
                        comparator.compare(a, d) >= 0 ? a : d
                        : comparator.compare(c, d) >= 0 ? c : d
                : comparator.compare(b, c) >= 0 ?
                comparator.compare(b, d) >= 0 ? b : d
                : comparator.compare(c, d) >= 0 ? c : d;
    }
}
