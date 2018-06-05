/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public class ListBuilder<E, L extends List<E>> {

    private L mList;

    private ListBuilder(L list) {
        mList = list;
    }

    public static <E> ListBuilder<E, ArrayList<E>> newArrayList() {
        return new ListBuilder<>(new ArrayList<>());
    }

    public static <E, L extends List<E>> ListBuilder<E, L> buildUpon(L List) {
        return new ListBuilder<>(List);
    }

    public L build() {
        L list = mList;
        mList = null;
        return list;
    }

    public List<E> buildUnmodifiable() {
        List<E> List = Collections.unmodifiableList(mList);
        mList = null;
        return List;
    }


    public ListBuilder<E, L> add(E element) {
        mList.add(element);
        return this;
    }

    public ListBuilder<E, L> remove(Object element) {
        mList.remove(element);
        return this;
    }

    public ListBuilder<E, L> addAll(@NonNull Collection<? extends E> collection) {
        mList.addAll(collection);
        return this;
    }

    public ListBuilder<E, L> addAll(int index, @NonNull Collection<? extends E> collection) {
        mList.addAll(index, collection);
        return this;
    }

    public ListBuilder<E, L> removeAll(@NonNull Collection<?> collection) {
        mList.removeAll(collection);
        return this;
    }

    public ListBuilder<E, L> retainAll(@NonNull Collection<?> collection) {
        mList.retainAll(collection);
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public ListBuilder<E, L> replaceAll(UnaryOperator<E> operator) {
        mList.replaceAll(operator);
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public ListBuilder<E, L> sort(Comparator<? super E> comparator) {
        mList.sort(comparator);
        return this;
    }

    public ListBuilder<E, L> clear() {
        mList.clear();
        return this;
    }

    public ListBuilder<E, L> set(int index, E element) {
        mList.set(index, element);
        return this;
    }

    public ListBuilder<E, L> add(int index, E element) {
        mList.add(index, element);
        return this;
    }

    public ListBuilder<E, L> remove(int index) {
        mList.remove(index);
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public ListBuilder<E, L> removeIf(Predicate<? super E> filter) {
        mList.removeIf(filter);
        return this;
    }
}
