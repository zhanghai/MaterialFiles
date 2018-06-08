/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class SetBuilder<E, L extends Set<E>> {

    private L mSet;

    private SetBuilder(L set) {
        mSet = set;
    }

    public static <E> SetBuilder<E, HashSet<E>> newHashSet() {
        return new SetBuilder<>(new HashSet<>());
    }

    public static <E, L extends Set<E>> SetBuilder<E, L> buildUpon(L Set) {
        return new SetBuilder<>(Set);
    }

    public L build() {
        L set = mSet;
        mSet = null;
        return set;
    }

    public Set<E> buildUnmodifiable() {
        Set<E> Set = Collections.unmodifiableSet(mSet);
        mSet = null;
        return Set;
    }


    public SetBuilder<E, L> add(E element) {
        mSet.add(element);
        return this;
    }

    public SetBuilder<E, L> remove(Object element) {
        mSet.remove(element);
        return this;
    }

    public SetBuilder<E, L> addAll(@NonNull Collection<? extends E> collection) {
        mSet.addAll(collection);
        return this;
    }

    public SetBuilder<E, L> retainAll(@NonNull Collection<?> collection) {
        mSet.retainAll(collection);
        return this;
    }

    public SetBuilder<E, L> removeAll(@NonNull Collection<?> collection) {
        mSet.removeAll(collection);
        return this;
    }

    public SetBuilder<E, L> clear() {
        mSet.clear();
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public SetBuilder<E, L> removeIf(Predicate<? super E> filter) {
        mSet.removeIf(filter);
        return this;
    }
}
