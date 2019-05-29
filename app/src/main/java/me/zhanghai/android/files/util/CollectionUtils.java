/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import java.util.AbstractList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;
import me.zhanghai.android.files.functional.Functional;
import me.zhanghai.android.files.functional.FunctionalIterator;

public class CollectionUtils {

    private CollectionUtils() {}

    @Nullable
    public static <E> E first(@NonNull List<? extends E> list) {
        return list.get(0);
    }

    @Nullable
    public static <E> E last(@NonNull List<? extends E> list) {
        return list.get(list.size() - 1);
    }

    @Nullable
    public static <E> E firstOrNull(@NonNull List<? extends E> list) {
        return getOrNull(list, 0);
    }

    @Nullable
    public static <E> E lastOrNull(@NonNull List<? extends E> list) {
        return getOrNull(list, list.size() - 1);
    }

    @Nullable
    public static <E> E getOrNull(@NonNull List<? extends E> list, int index) {
        return index >= 0 && index < list.size() ? list.get(index) : null;
    }

    @Nullable
    public static <E> E first(@NonNull Collection<? extends E> collection) {
        return collection.iterator().next();
    }

    @Nullable
    public static <E> E firstOrNull(@NonNull Collection<? extends E> collection) {
        return collection.size() > 0 ? first(collection) : null;
    }

    @Nullable
    public static <E> E peek(@NonNull List<? extends E> list) {
        return lastOrNull(list);
    }

    public static <E> void push(@NonNull List<? super E> list, E item) {
        list.add(item);
    }

    @Nullable
    public static <E> E pop(@NonNull List<? extends E> list) {
        return list.remove(list.size() - 1);
    }

    @Nullable
    public static <E> E popOrNull(@NonNull List<? extends E> list) {
        return !list.isEmpty() ? pop(list) : null;
    }

    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static int size(@Nullable Collection<?> collection) {
        return collection != null ? collection.size() : 0;
    }

    public static <E> boolean startsWith(@NonNull List<? extends E> list1,
                                         @NonNull List<? extends E> list2) {
        return list1.size() >= list2.size() && Functional.every(list2, (element, index) ->
                ObjectsCompat.equals(list1.get(index), element));
    }

    public static <E> boolean endsWith(@NonNull List<? extends E> list1,
                                       @NonNull List<? extends E> list2) {
        int list1Size = list1.size();
        return list1Size >= list2.size() && FunctionalIterator.everyRemaining(
                new FunctionalIterator.ReverseIterator<>(list2), (element, index) ->
                        ObjectsCompat.equals(list1.get(list1Size - 1 - index), element));
    }

    @NonNull
    public static <E> Set<E> difference(@NonNull Set<? extends E> set1,
                                        @NonNull Set<? extends E> set2) {
        Set<E> result = new HashSet<>();
        difference(set1, set2, result);
        return result;
    }

    @NonNull
    public static <E> Set<E> symmetricDifference(@NonNull Set<? extends E> set1,
                                                 @NonNull Set<? extends E> set2) {
        Set<E> result = new HashSet<>();
        difference(set1, set2, result);
        difference(set2, set1, result);
        return result;
    }

    private static <E> void difference(@NonNull Set<? extends E> set1,
                                       @NonNull Set<? extends E> set2, @NonNull Set<E> result) {
        for (E element : set1) {
            if (!set2.contains(element)) {
                result.add(element);
            }
        }
    }

    @NonNull
    public static <E> List<E> union(@NonNull List<? extends E> list1,
                                    @NonNull List<? extends E> list2) {
        if (list1 instanceof RandomAccess && list2 instanceof RandomAccess) {
            return new RandomAccessUnionList<>(list1, list2);
        } else {
            return new UnionList<>(list1, list2);
        }
    }

    private static class UnionList<E> extends AbstractList<E> {

        @NonNull
        private final List<? extends E> mList1;
        @NonNull
        private final List<? extends E> mList2;

        public UnionList(@NonNull List<? extends E> list1, @NonNull List<? extends E> list2) {
            mList1 = list1;
            mList2 = list2;
        }

        @Nullable
        @Override
        public E get(int location) {
            int list1Size = mList1.size();
            return location < list1Size ? mList1.get(location) : mList2.get(location - list1Size);
        }

        @Override
        public int size() {
            return mList1.size() + mList2.size();
        }
    }

    private static class RandomAccessUnionList<E> extends UnionList<E> implements RandomAccess {

        public RandomAccessUnionList(@NonNull List<? extends E> list1,
                                     @NonNull List<? extends E> list2) {
            super(list1, list2);
        }
    }
}
