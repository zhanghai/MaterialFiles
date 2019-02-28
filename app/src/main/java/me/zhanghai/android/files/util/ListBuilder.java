/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.os.Build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

@SuppressWarnings("unused")
public class ListBuilder<E, L extends List<E>> {

    @NonNull
    private L mList;

    private ListBuilder(@NonNull L list) {
        mList = list;
    }

    @NonNull
    public static <E> ListBuilder<E, ArrayList<E>> newArrayList() {
        return new ListBuilder<>(new ArrayList<>());
    }

    @NonNull
    public static <E, L extends List<E>> ListBuilder<E, L> buildUpon(@NonNull L List) {
        return new ListBuilder<>(List);
    }

    @NonNull
    public L build() {
        L list = mList;
        mList = null;
        return list;
    }

    @NonNull
     public List<E> buildUnmodifiable() {
        List<E> List = Collections.unmodifiableList(mList);
        mList = null;
        return List;
    }


    @NonNull
    public ListBuilder<E, L> add(@Nullable E element) {
        mList.add(element);
        return this;
    }

    @NonNull
    public ListBuilder<E, L> remove(@Nullable Object element) {
        mList.remove(element);
        return this;
    }

    @NonNull
    public ListBuilder<E, L> addAll(@NonNull Collection<? extends E> collection) {
        mList.addAll(collection);
        return this;
    }

    @NonNull
    public ListBuilder<E, L> addAll(int index, @NonNull Collection<? extends E> collection) {
        mList.addAll(index, collection);
        return this;
    }

    @NonNull
    public ListBuilder<E, L> removeAll(@NonNull Collection<?> collection) {
        mList.removeAll(collection);
        return this;
    }

    @NonNull
    public ListBuilder<E, L> retainAll(@NonNull Collection<?> collection) {
        mList.retainAll(collection);
        return this;
    }

    @NonNull
    @RequiresApi(Build.VERSION_CODES.N)
    public ListBuilder<E, L> replaceAll(@NonNull UnaryOperator<E> operator) {
        mList.replaceAll(operator);
        return this;
    }

    @NonNull
    @RequiresApi(Build.VERSION_CODES.N)
    public ListBuilder<E, L> sort(@Nullable Comparator<? super E> comparator) {
        mList.sort(comparator);
        return this;
    }

    @NonNull
    public ListBuilder<E, L> clear() {
        mList.clear();
        return this;
    }

    @NonNull
    public ListBuilder<E, L> set(int index, @Nullable E element) {
        mList.set(index, element);
        return this;
    }

    @NonNull
    public ListBuilder<E, L> add(int index, @Nullable E element) {
        mList.add(index, element);
        return this;
    }

    @NonNull
    public ListBuilder<E, L> remove(int index) {
        mList.remove(index);
        return this;
    }

    @NonNull
    @RequiresApi(Build.VERSION_CODES.N)
    public ListBuilder<E, L> removeIf(@NonNull Predicate<? super E> filter) {
        mList.removeIf(filter);
        return this;
    }
}
