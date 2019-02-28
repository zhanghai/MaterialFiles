/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.os.Build;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

@SuppressWarnings("unused")
public class SetBuilder<E, L extends Set<E>> {

    @NonNull
    private L mSet;

    private SetBuilder(@NonNull L set) {
        mSet = set;
    }

    @NonNull
    public static <E> SetBuilder<E, HashSet<E>> newHashSet() {
        return new SetBuilder<>(new HashSet<>());
    }

    @NonNull
    public static <E, L extends Set<E>> SetBuilder<E, L> buildUpon(@NonNull L Set) {
        return new SetBuilder<>(Set);
    }

    @NonNull
    public L build() {
        L set = mSet;
        mSet = null;
        return set;
    }

    @NonNull
    public Set<E> buildUnmodifiable() {
        Set<E> Set = Collections.unmodifiableSet(mSet);
        mSet = null;
        return Set;
    }


    @NonNull
    public SetBuilder<E, L> add(@Nullable E element) {
        mSet.add(element);
        return this;
    }

    @NonNull
    public SetBuilder<E, L> remove(@Nullable E element) {
        mSet.remove(element);
        return this;
    }

    @NonNull
    public SetBuilder<E, L> addAll(@NonNull Collection<? extends E> collection) {
        mSet.addAll(collection);
        return this;
    }

    @NonNull
    public SetBuilder<E, L> retainAll(@NonNull Collection<?> collection) {
        mSet.retainAll(collection);
        return this;
    }

    @NonNull
    public SetBuilder<E, L> removeAll(@NonNull Collection<?> collection) {
        mSet.removeAll(collection);
        return this;
    }

    @NonNull
    public SetBuilder<E, L> clear() {
        mSet.clear();
        return this;
    }

    @NonNull
    @RequiresApi(Build.VERSION_CODES.N)
    public SetBuilder<E, L> removeIf(@NonNull Predicate<? super E> filter) {
        mSet.removeIf(filter);
        return this;
    }
}
