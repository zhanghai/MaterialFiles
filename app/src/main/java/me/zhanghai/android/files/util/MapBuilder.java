/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.os.Build;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

@SuppressWarnings("unused")
public class MapBuilder<K, V, M extends Map<K, V>> {

    @NonNull
    private M mMap;

    private MapBuilder(@NonNull M map) {
        mMap = map;
    }

    @NonNull
    public static <K, V> MapBuilder<K, V, HashMap<K, V>> newHashMap() {
        return new MapBuilder<>(new HashMap<>());
    }

    @NonNull
    public static <K, V, M extends Map<K, V>> MapBuilder<K, V, M> buildUpon(@NonNull M map) {
        return new MapBuilder<>(map);
    }

    @NonNull
    public M build() {
        M map = mMap;
        mMap = null;
        return map;
    }

    @NonNull
    public Map<K, V> buildUnmodifiable() {
        Map<K, V> map = Collections.unmodifiableMap(mMap);
        mMap = null;
        return map;
    }


    @NonNull
    public MapBuilder<K, V, M> put(@Nullable K key, @Nullable V value) {
        mMap.put(key, value);
        return this;
    }

    @NonNull
    public MapBuilder<K, V, M> remove(@Nullable K key) {
        mMap.remove(key);
        return this;
    }

    @NonNull
    public MapBuilder<K, V, M> putAll(@NonNull Map<? extends K, ? extends V> m) {
        mMap.putAll(m);
        return this;
    }

    @NonNull
    public MapBuilder<K, V, M> clear() {
        mMap.clear();
        return this;
    }

    @NonNull
    @RequiresApi(Build.VERSION_CODES.N)
    public MapBuilder<K, V, M> replaceAll(
            @NonNull BiFunction<? super K, ? super V, ? extends V> function) {
        mMap.replaceAll(function);
        return this;
    }

    @NonNull
    @RequiresApi(Build.VERSION_CODES.N)
    public MapBuilder<K, V, M> putIfAbsent(@Nullable K key, @Nullable V value) {
        mMap.putIfAbsent(key, value);
        return this;
    }

    @NonNull
    @RequiresApi(Build.VERSION_CODES.N)
    public MapBuilder<K, V, M> remove(@Nullable K key, @Nullable V value) {
        mMap.remove(key, value);
        return this;
    }

    @NonNull
    @RequiresApi(Build.VERSION_CODES.N)
    public MapBuilder<K, V, M> replace(@Nullable K key, @Nullable V oldValue,
                                       @Nullable V newValue) {
        mMap.replace(key, oldValue, newValue);
        return this;
    }

    @NonNull
    @RequiresApi(Build.VERSION_CODES.N)
    public MapBuilder<K, V, M> replace(@Nullable K key, @Nullable V value) {
        mMap.replace(key, value);
        return this;
    }

    @NonNull
    @RequiresApi(Build.VERSION_CODES.N)
    public MapBuilder<K, V, M> merge(
            @Nullable K key, @NonNull V value,
            @NonNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        mMap.merge(key, value, remappingFunction);
        return this;
    }
}
