/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.functional.throwing;

import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface ThrowingTertiaryOperator<T> extends ThrowingTriFunction<T, T, T, T> {

    static <T> ThrowingTertiaryOperator<T> minBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b, c) -> comparator.compare(a, b) <= 0 ?
                comparator.compare(a, c) <= 0 ? a : c
                : comparator.compare(b, c) <= 0 ? b : c;
    }

    static <T> ThrowingTertiaryOperator<T> maxBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b, c) -> comparator.compare(a, b) >= 0 ?
                comparator.compare(a, c) >= 0 ? a : c
                : comparator.compare(b, c) >= 0 ? b : c;
    }
}
