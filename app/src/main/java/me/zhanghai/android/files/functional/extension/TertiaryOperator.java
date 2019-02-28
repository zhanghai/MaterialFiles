/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.functional.extension;

import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface TertiaryOperator<T> extends TriFunction<T, T, T, T> {

    static <T> TertiaryOperator<T> minBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b, c) -> comparator.compare(a, b) <= 0 ?
                comparator.compare(a, c) <= 0 ? a : c
                : comparator.compare(b, c) <= 0 ? b : c;
    }

    static <T> TertiaryOperator<T> maxBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b, c) -> comparator.compare(a, b) >= 0 ?
                comparator.compare(a, c) >= 0 ? a : c
                : comparator.compare(b, c) >= 0 ? b : c;
    }
}
