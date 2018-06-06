/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.support.v4.util.ObjectsCompat;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

import me.zhanghai.android.materialfilemanager.functional.Functional;

public class CollectionUtils {

    private CollectionUtils() {}

    public static <E> E firstOrNull(List<? extends E> list) {
        return getOrNull(list, 0);
    }

    public static <E> E lastOrNull(List<? extends E> list) {
        return getOrNull(list, list.size() - 1);
    }

    public static <E> E getOrNull(List<? extends E> list, int index) {
        return index >= 0 && index < list.size() ? list.get(index) : null;
    }

    public static <E> E peek(List<? extends E> list) {
        return lastOrNull(list);
    }

    public static <E> void push(List<? super E> list, E item) {
        list.add(item);
    }

    public static <E> E pop(List<? extends E> list) {
        return list.remove(list.size() - 1);
    }

    public static <E> E popOrNull(List<? extends E> list) {
        return !list.isEmpty() ? pop(list) : null;
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isPrefix(List<?> prefix, List<?> list) {
        return prefix.size() <= list.size() && Functional.every(prefix,
                (element, index) -> ObjectsCompat.equals(element, list.get(index)));
    }

    public static int size(Collection<?> collection) {
        return collection != null ? collection.size() : 0;
    }

    public static <E> List<E> union(List<E> list1, List<E> list2) {
        if (list1 instanceof RandomAccess && list2 instanceof RandomAccess) {
            return new RandomAccessUnionList<>(list1, list2);
        } else {
            return new UnionList<>(list1, list2);
        }
    }

    private static class UnionList<E> extends AbstractList<E> {

        private List<E> mList1;
        private List<E> mList2;

        public UnionList(List<E> list1, List<E> list2) {
            mList1 = list1;
            mList2 = list2;
        }

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

        public RandomAccessUnionList(List<E> list1, List<E> list2) {
            super(list1, list2);
        }
    }
}
