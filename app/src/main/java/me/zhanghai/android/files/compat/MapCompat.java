/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat;

import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java9.util.function.BiFunction;
import java9.util.function.Function;

@SuppressWarnings("unused")
public class MapCompat {

    private MapCompat() {}

    @Nullable
    public static <K, V> V getOrDefault(@NonNull Map<K, V> map, @Nullable K key,
                                        @Nullable V defaultValue) {
        V value = map.get(key);
        if (value != null || map.containsKey(key)) {
            return value;
        }
        return defaultValue;
    }

    @Nullable
    public static <K, V> V putIfAbsent(@NonNull Map<K, V> map, @Nullable K key,
                                       @Nullable V value) {
        V oldValue = map.get(key);
        if (oldValue == null) {
            oldValue = map.put(key, value);
        }
        return oldValue;
    }

    @Nullable
    public static <K, V> V computeIfAbsent(
            @NonNull Map<K, V> map, @Nullable K key,
            @NonNull Function<? super K, ? extends V> mappingFunction) {
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

    @Nullable
    public static <K, V> V computeIfPresent(
            @NonNull Map<K, V> map, @Nullable K key,
            @NonNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
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

    @Nullable
    public static <K, V> V compute(
            @NonNull Map<K, V> map, @Nullable K key,
            @NonNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
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

    @Nullable
    public static <K, V> V merge(
            @NonNull Map<K, V> map, @Nullable K key, @NonNull V value,
            @NonNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
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
