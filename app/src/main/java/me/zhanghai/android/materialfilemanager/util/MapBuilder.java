/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.ArrayMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class MapBuilder<K, V, M extends Map<K, V>> {

    private M mMap;

    private MapBuilder(M map) {
        mMap = map;
    }

    public static <K, V> MapBuilder<K, V, HashMap<K, V>> newHashMap() {
        return new MapBuilder<>(new HashMap<>());
    }

    public static <K, V, M extends Map<K, V>> MapBuilder<K, V, M> buildUpon(M map) {
        return new MapBuilder<>(map);
    }

    public M build() {
        M map = mMap;
        mMap = null;
        return map;
    }

    public Map<K, V> buildUnmodifiable() {
        Map<K, V> map = Collections.unmodifiableMap(mMap);
        mMap = null;
        return map;
    }


    public MapBuilder<K, V, M> put(K key, V value) {
        mMap.put(key, value);
        return this;
    }

    public MapBuilder<K, V, M> remove(Object key) {
        mMap.remove(key);
        return this;
    }

    public MapBuilder<K, V, M> putAll(@NonNull Map<? extends K, ? extends V> m) {
        mMap.putAll(m);
        return this;
    }

    public MapBuilder<K, V, M> clear() {
        mMap.clear();
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public MapBuilder<K, V, M> replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        mMap.replaceAll(function);
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public MapBuilder<K, V, M> putIfAbsent(K key, V value) {
        mMap.putIfAbsent(key, value);
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public MapBuilder<K, V, M> remove(Object key, Object value) {
        mMap.remove(key, value);
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public MapBuilder<K, V, M> replace(K key, V oldValue, V newValue) {
        mMap.replace(key, oldValue, newValue);
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public MapBuilder<K, V, M> replace(K key, V value) {
        mMap.replace(key, value);
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public MapBuilder<K, V, M> merge(K key, V value,
                                  BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        mMap.merge(key, value, remappingFunction);
        return this;
    }
}
