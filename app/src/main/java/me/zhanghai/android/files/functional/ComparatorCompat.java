/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.functional;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import me.zhanghai.android.files.functional.compat.Function;
import me.zhanghai.android.files.functional.compat.ToDoubleFunction;
import me.zhanghai.android.files.functional.compat.ToIntFunction;
import me.zhanghai.android.files.functional.compat.ToLongFunction;

@SuppressWarnings("unused")
public class ComparatorCompat {

    private ComparatorCompat() {}

    public static <T> Comparator<T> reversed(Comparator<T> comparator) {
        return Collections.reverseOrder(comparator);
    }

    public static <T> Comparator<T> thenComparing(Comparator<T> comparator,
                                                  Comparator<? super T> other) {
        Objects.requireNonNull(other);
        return (Comparator<T> & Serializable) (object1, object2) -> {
            int result = comparator.compare(object1, object2);
            return (result != 0) ? result : other.compare(object1, object2);
        };
    }

    public static <T, U> Comparator<T> thenComparing(Comparator<T> comparator,
                                                     Function<? super T, ? extends U> keyExtractor,
                                                     Comparator<? super U> keyComparator) {
        return thenComparing(comparator, comparing(keyExtractor, keyComparator));
    }

    public static <T, U extends Comparable<? super U>> Comparator<T> thenComparing(
            Comparator<T> comparator, Function<? super T, ? extends U> keyExtractor) {
        return thenComparing(comparator, comparing(keyExtractor));
    }

    public static <T> Comparator<T> thenComparingInt(Comparator<T> comparator,
                                                     ToIntFunction<? super T> keyExtractor) {
        return thenComparing(comparator, comparingInt(keyExtractor));
    }

    public static <T> Comparator<T> thenComparingLong(Comparator<T> comparator,
                                                      ToLongFunction<? super T> keyExtractor) {
        return thenComparing(comparator, comparingLong(keyExtractor));
    }

    public static <T> Comparator<T> thenComparingDouble(Comparator<T> comparator,
                                                      ToDoubleFunction<? super T> keyExtractor) {
        return thenComparing(comparator, comparingDouble(keyExtractor));
    }

    public static <T extends Comparable<? super T>> Comparator<T> reverseOrder() {
        return Collections.reverseOrder();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> Comparator<T> naturalOrder() {
        return (Comparator<T>) Comparators.NaturalOrderComparator.INSTANCE;
    }

    public static <T> Comparator<T> nullsFirst(Comparator<? super T> comparator) {
        return new Comparators.NullComparator<>(true, comparator);
    }

    public static <T> Comparator<T> nullsLast(Comparator<? super T> comparator) {
        return new Comparators.NullComparator<>(false, comparator);
    }

    public static <T, U> Comparator<T> comparing(Function<? super T, ? extends U> keyExtractor,
                                                 Comparator<? super U> keyComparator) {
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(keyComparator);
        return (Comparator<T> & Serializable) (object1, object2) ->
                keyComparator.compare(keyExtractor.apply(object1), keyExtractor.apply(object2));
    }

    public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
            Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (Comparator<T> & Serializable) (object1, object2) ->
                keyExtractor.apply(object1).compareTo(keyExtractor.apply(object2));
    }

    public static <T> Comparator<T> comparingInt(ToIntFunction<? super T> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (Comparator<T> & Serializable) (object1, object2) ->
                Integer.compare(keyExtractor.applyAsInt(object1), keyExtractor.applyAsInt(object2));
    }

    public static <T> Comparator<T> comparingLong(ToLongFunction<? super T> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (Comparator<T> & Serializable) (object1, object2) ->
                Long.compare(keyExtractor.applyAsLong(object1), keyExtractor.applyAsLong(object2));
    }

    public static<T> Comparator<T> comparingDouble(ToDoubleFunction<? super T> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (Comparator<T> & Serializable) (object1, object2) ->
                Double.compare(keyExtractor.applyAsDouble(object1),
                        keyExtractor.applyAsDouble(object2));
    }

    private static class Comparators {

        private Comparators() {}

        public enum NaturalOrderComparator implements Comparator<Comparable<Object>> {

            INSTANCE;

            @Override
            public int compare(Comparable<Object> object1, Comparable<Object> object2) {
                return object1.compareTo(object2);
            }

            @Override
            public Comparator<Comparable<Object>> reversed() {
                return ComparatorCompat.reverseOrder();
            }
        }

        public static class NullComparator<T> implements Comparator<T>, Serializable {

            private static final long serialVersionUID = -6860745584223624556L;

            private final boolean nullFirst;
            private final Comparator<T> real;

            @SuppressWarnings("unchecked")
            public NullComparator(boolean nullFirst, Comparator<? super T> real) {
                this.nullFirst = nullFirst;
                this.real = (Comparator<T>) real;
            }

            @Override
            public int compare(T object1, T object2) {
                if (object1 == null) {
                    return object2 == null ? 0 : nullFirst ? -1 : 1;
                } else if (object2 == null) {
                    return nullFirst ? 1: -1;
                } else {
                    return real == null ? 0 : real.compare(object1, object2);
                }
            }

            @Override
            public Comparator<T> thenComparing(Comparator<? super T> other) {
                Objects.requireNonNull(other);
                return new NullComparator<>(nullFirst, real == null ? other
                        : ComparatorCompat.thenComparing(real, other));
            }

            @Override
            public Comparator<T> reversed() {
                return new NullComparator<>(!nullFirst, real == null ? null
                        : ComparatorCompat.reversed(real));
            }
        }
    }
}
