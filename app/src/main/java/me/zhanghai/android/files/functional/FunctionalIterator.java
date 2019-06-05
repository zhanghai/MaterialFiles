/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.functional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import androidx.annotation.CheckResult;
import java9.util.Iterators;
import java9.util.function.BiConsumer;
import java9.util.function.BiFunction;
import java9.util.function.BiPredicate;
import java9.util.function.Consumer;
import java9.util.function.Function;
import java9.util.function.Predicate;
import me.zhanghai.android.files.functional.extension.QuadFunction;
import me.zhanghai.android.files.functional.extension.TriConsumer;
import me.zhanghai.android.files.functional.extension.TriFunction;
import me.zhanghai.android.files.functional.extension.TriPredicate;

public class FunctionalIterator {

    private FunctionalIterator() {}

    public static <T> boolean everyRemaining(Iterator<T> iterator, Predicate<T> predicate) {
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (!predicate.test(t)) {
                return false;
            }
        }
        return true;
    }

    public static <T> boolean everyRemaining(Iterator<T> iterator,
                                             BiPredicate<T, Integer> predicate) {
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (!predicate.test(t, index)) {
                return false;
            }
            ++index;
        }
        return true;
    }

    public static <I extends Iterator<T>, T> boolean everyRemaining(
            I iterator, TriPredicate<T, Integer, I> predicate) {
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (!predicate.test(t, index, iterator)) {
                return false;
            }
            ++index;
        }
        return true;
    }

    public static <T, J extends Collection<? super T>> J filterRemaining(Iterator<T> iterator,
                                                                         Predicate<T> predicate,
                                                                         J collector) {
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (predicate.test(t)) {
                collector.add(t);
            }
        }
        return collector;
    }

    @CheckResult
    public static <T> ArrayList<T> filterRemaining(Iterator<T> iterator, Predicate<T> predicate) {
        return filterRemaining(iterator, predicate, new ArrayList<>());
    }

    public static <T, J extends Collection<? super T>> J filterRemaining(
            Iterator<T> iterator, BiPredicate<T, Integer> predicate, J collector) {
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (predicate.test(t, index)) {
                collector.add(t);
            }
            ++index;
        }
        return collector;
    }

    @CheckResult
    public static <T> ArrayList<T> filterRemaining(Iterator<T> iterator,
                                                   BiPredicate<T, Integer> predicate) {
        return filterRemaining(iterator, predicate, new ArrayList<>());
    }

    public static <I extends Iterator<T>, T, J extends Collection<? super T>> J filterRemaining(
            I iterator, TriPredicate<T, Integer, I> predicate, J collector) {
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (predicate.test(t, index, iterator)) {
                collector.add(t);
            }
            ++index;
        }
        return collector;
    }

    @CheckResult
    public static <I extends Iterator<T>, T> ArrayList<T> filterRemaining(
            I iterator, TriPredicate<T, Integer, I> predicate) {
        return filterRemaining(iterator, predicate, new ArrayList<>());
    }

    public static <T> T findRemaining(Iterator<T> iterator, Predicate<T> predicate) {
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (predicate.test(t)) {
                return t;
            }
        }
        return null;
    }

    public static <T> T findRemaining(Iterator<T> iterator, BiPredicate<T, Integer> predicate) {
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (predicate.test(t, index)) {
                return t;
            }
            ++index;
        }
        return null;
    }

    public static <I extends Iterator<T>, T> T findRemaining(
            I iterator, TriPredicate<T, Integer, I> predicate) {
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (predicate.test(t, index, iterator)) {
                return t;
            }
            ++index;
        }
        return null;
    }

    public static <T> int findIndexRemaining(Iterator<T> iterator, Predicate<T> predicate) {
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (predicate.test(t)) {
                return index;
            }
            ++index;
        }
        return -1;
    }

    public static <T> int findIndexRemaining(Iterator<T> iterator,
                                             BiPredicate<T, Integer> predicate) {
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (predicate.test(t, index)) {
                return index;
            }
            ++index;
        }
        return -1;
    }

    public static <I extends Iterator<T>, T> int findIndexRemaining(
            I iterator, TriPredicate<T, Integer, I> predicate) {
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (predicate.test(t, index, iterator)) {
                return index;
            }
            ++index;
        }
        return -1;
    }

    public static <T> void forEachRemaining(Iterator<T> iterator, Consumer<T> consumer) {
        Iterators.forEachRemaining(iterator, consumer);
    }

    public static <T> void forEachRemaining(Iterator<T> iterator, BiConsumer<T, Integer> consumer) {
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            consumer.accept(t, index);
            ++index;
        }
    }

    public static <I extends Iterator<T>, T> void forEachRemaining(
            I iterator, TriConsumer<T, Integer, I> consumer) {
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            consumer.accept(t, index, iterator);
            ++index;
        }
    }

    public static <T, U, J extends Collection<? super U>> J mapRemaining(Iterator<T> iterator,
                                                                         Function<T, U> function,
                                                                         J collector) {
        while (iterator.hasNext()) {
            T t = iterator.next();
            collector.add(function.apply(t));
        }
        return collector;
    }

    @CheckResult
    public static <T, U> ArrayList<U> mapRemaining(Iterator<T> iterator, Function<T, U> function) {
        return mapRemaining(iterator, function, new ArrayList<>());
    }

    public static <T, U, J extends Collection<? super U>> J mapRemaining(
            Iterator<T> iterator, BiFunction<T, Integer, U> function, J collector) {
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            collector.add(function.apply(t, index));
            ++index;
        }
        return collector;
    }

    @CheckResult
    public static <T, U> ArrayList<U> mapRemaining(Iterator<T> iterator,
                                                   BiFunction<T, Integer, U> function) {
        return mapRemaining(iterator, function, new ArrayList<>());
    }

    public static <I extends Iterator<T>, T, U, J extends Collection<? super U>> J mapRemaining(
            I iterator, TriFunction<T, Integer, I, U> function, J collector) {
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            collector.add(function.apply(t, index, iterator));
            ++index;
        }
        return collector;
    }

    @CheckResult
    public static <I extends Iterator<T>, T, U> ArrayList<U> mapRemaining(
            I iterator, TriFunction<T, Integer, I, U> function) {
        return mapRemaining(iterator, function, new ArrayList<>());
    }

    public static <T, U> U reduceRemaining(Iterator<T> iterator, BiFunction<U, T, U> function,
                                           U initialValue) {
        U accumulator = initialValue;
        while (iterator.hasNext()) {
            T t = iterator.next();
            accumulator = function.apply(accumulator, t);
        }
        return accumulator;
    }

    public static <T, U> U reduceRemaining(Iterator<T> iterator,
                                           TriFunction<U, T, Integer, U> function, U initialValue) {
        U accumulator = initialValue;
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            accumulator = function.apply(accumulator, t, index);
            ++index;
        }
        return accumulator;
    }

    public static <I extends Iterator<T>, T, U> U reduceRemaining(
            I iterator, QuadFunction<U, T, Integer, I, U> function, U initialValue) {
        U accumulator = initialValue;
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            accumulator = function.apply(accumulator, t, index, iterator);
            ++index;
        }
        return accumulator;
    }

    public static <T> T reduceRemaining(Iterator<T> iterator, BiFunction<T, T, T> function) {
        if (!iterator.hasNext()) {
            throw new IllegalArgumentException("Reduce of empty iterator with no initial value");
        }
        return reduceRemaining(iterator, function, iterator.next());
    }

    public static <T> T reduceRemaining(Iterator<T> iterator,
                                        TriFunction<T, T, Integer, T> function) {
        if (!iterator.hasNext()) {
            throw new IllegalArgumentException("Reduce of empty iterator with no initial value");
        }
        return reduceRemaining(iterator, (accumulator, t, index) ->
                function.apply(accumulator, t, index + 1), iterator.next());
    }

    public static <I extends Iterator<T>, T> T reduceRemaining(
            I iterator, QuadFunction<T, T, Integer, I, T> function) {
        if (!iterator.hasNext()) {
            throw new IllegalArgumentException("Reduce of empty iterator with no initial value");
        }
        return reduceRemaining(iterator, (accumulator, t, index, iterator2) ->
                function.apply(accumulator, t, index + 1, iterator2), iterator.next());
    }

    public static <T> boolean someRemaining(Iterator<T> iterator, Predicate<T> predicate) {
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (predicate.test(t)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean someRemaining(Iterator<T> iterator,
                                            BiPredicate<T, Integer> predicate) {
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (predicate.test(t, index)) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public static <I extends Iterator<T>, T> boolean someRemaining(
            I iterator, TriPredicate<T, Integer, I> predicate) {
        int index = 0;
        while (iterator.hasNext()) {
            T t = iterator.next();
            if (predicate.test(t, index, iterator)) {
                return true;
            }
            ++index;
        }
        return false;
    }

    public static class ReverseIterator<T> implements Iterator<T> {

        private ListIterator<T> mListIterator;

        public ReverseIterator(List<T> list) {
            mListIterator = list.listIterator(list.size());
        }

        @Override
        public boolean hasNext() {
            return mListIterator.hasPrevious();
        }

        @Override
        public T next() {
            return mListIterator.previous();
        }
    }
}
