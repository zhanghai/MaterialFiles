/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;
import me.zhanghai.java.functional.Functional;
import me.zhanghai.java.functional.FunctionalIterator;

public class CollectionUtils {

    private CollectionUtils() {}

    public static <E> E first(@NonNull List<? extends E> list) {
        return list.get(0);
    }

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

    public static <E> void push(@NonNull List<? super E> list, E element) {
        list.add(element);
    }

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
    public static <E> LinkedHashSet<E> singletonLinkedSet(@Nullable E element) {
        LinkedHashSet<E> set = new LinkedHashSet<>(1, 1);
        set.add(element);
        return set;
    }

    @Nullable
    public static <E> Set<E> singletonOrNull(@Nullable E element) {
        return element != null ? Collections.singleton(element) : null;
    }

    @Nullable
    public static <E> List<E> singletonListOrNull(@Nullable E element) {
        return element != null ? Collections.singletonList(element) : null;
    }

    @NonNull
    public static <E> Set<E> singletonOrEmpty(@Nullable E element) {
        return element != null ? Collections.singleton(element) : Collections.emptySet();
    }

    @NonNull
    public static <E> List<E> singletonListOrEmpty(@Nullable E element) {
        return element != null ? Collections.singletonList(element) : Collections.emptyList();
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
    public static <E> List<E> join(@NonNull List<? extends E> list1,
                                   @NonNull List<? extends E> list2) {
        if (list1 instanceof RandomAccess && list2 instanceof RandomAccess) {
            return new RandomAccessJoinedList<>(list1, list2);
        } else {
            return new JoinedList<>(list1, list2);
        }
    }

    private static class JoinedList<E> extends AbstractList<E> {

        @NonNull
        private final List<? extends E> mList1;
        @NonNull
        private final List<? extends E> mList2;

        public JoinedList(@NonNull List<? extends E> list1, @NonNull List<? extends E> list2) {
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

    private static class RandomAccessJoinedList<E> extends JoinedList<E> implements RandomAccess {

        public RandomAccessJoinedList(@NonNull List<? extends E> list1,
                                      @NonNull List<? extends E> list2) {
            super(list1, list2);
        }
    }

    @NonNull
    public static <E> ArrayList<E> toArrayList(@NonNull List<E> list) {
        return list instanceof ArrayList ? (ArrayList<E>) list : new ArrayList<>(list);
    }

    public static <E> ArrayList<E> toArrayListOrNull(@Nullable List<E> list) {
        return list != null ? toArrayList(list) : null;
    }
}
