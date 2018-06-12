/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import java.util.Map;
import java.util.Objects;

import me.zhanghai.android.materialfilemanager.functional.compat.BiFunction;
import me.zhanghai.android.materialfilemanager.functional.compat.Function;

@SuppressWarnings("unused")
public class MapCompat {

    private MapCompat() {}

    @SuppressWarnings("SuspiciousMethodCalls")
    public static <K, V> V getOrDefault(Map<K, V> map, Object key, V defaultValue) {
        V value = map.get(key);
        if (value != null || map.containsKey(key)) {
            return value;
        }
        return defaultValue;
    }

    public static <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
        V oldValue = map.get(key);
        if (oldValue == null) {
            oldValue = map.put(key, value);
        }
        return oldValue;
    }

    public static <K, V> V computeIfAbsent(Map<K, V> map, K key,
                                           Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        V value = map.get(key);
        if (value == null) {
            V newValue = mappingFunction.apply(key);
            if (newValue != null) {
                map.put(key, newValue);
                return newValue;
            }
        }
        return value;
    }

    public static <K, V> V computeIfPresent(
            Map<K, V> map, K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        V oldValue = map.get(key);
        if (oldValue != null) {
            V newValue = remappingFunction.apply(key, oldValue);
            if (newValue != null) {
                map.put(key, newValue);
                return newValue;
            } else {
                map.remove(key);
                return null;
            }
        } else {
            return null;
        }
    }

    public static <K, V> V compute(
            Map<K, V> map, K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        V oldValue = map.get(key);
        V newValue = remappingFunction.apply(key, oldValue);
        if (newValue != null) {
            map.put(key, newValue);
            return newValue;
        } else {
            if (oldValue != null || map.containsKey(key)) {
                map.remove(key);
            }
            return null;
        }
    }

    public static <K, V> V merge(Map<K, V> map, K key, V value,
                                 BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        V oldValue = map.get(key);
        V newValue = oldValue != null ? remappingFunction.apply(oldValue, value) : value;
        if (newValue != null) {
            map.put(key, newValue);
        } else {
            map.remove(key);
        }
        return newValue;
    }
}
