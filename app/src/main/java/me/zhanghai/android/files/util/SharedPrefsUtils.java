/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.content.SharedPreferences;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import me.zhanghai.android.files.AppApplication;

public class SharedPrefsUtils {

    private SharedPrefsUtils() {}

    @NonNull
    public static SharedPreferences getSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(AppApplication.getInstance());
    }

    @Nullable
    public static String getString(@NonNull Entry<String> entry) {
        return getSharedPrefs().getString(entry.getKey(), entry.getDefaultValue());
    }

    @Nullable
    public static Set<String> getStringSet(@NonNull Entry<Set<String>> entry) {
        return getSharedPrefs().getStringSet(entry.getKey(), entry.getDefaultValue());
    }

    public static int getInt(@NonNull Entry<Integer> entry) {
        return getSharedPrefs().getInt(entry.getKey(), entry.getDefaultValue());
    }

    public static long getLong(@NonNull Entry<Long> entry) {
        return getSharedPrefs().getLong(entry.getKey(), entry.getDefaultValue());
    }

    public static float getFloat(@NonNull Entry<Float> entry) {
        return getSharedPrefs().getFloat(entry.getKey(), entry.getDefaultValue());
    }

    public static boolean getBoolean(@NonNull Entry<Boolean> entry) {
        return getSharedPrefs().getBoolean(entry.getKey(), entry.getDefaultValue());
    }

    @NonNull
    public static SharedPreferences.Editor getEditor() {
        return getSharedPrefs().edit();
    }

    public static void putString(@NonNull Entry<String> entry, @Nullable String value) {
        getEditor().putString(entry.getKey(), value).apply();
    }

    public static void putStringSet(@NonNull Entry<Set<String>> entry,
                                    @Nullable Set<String> value) {
        getEditor().putStringSet(entry.getKey(), value).apply();
    }

    public static void putInt(@NonNull Entry<Integer> entry, int value) {
        getEditor().putInt(entry.getKey(), value).apply();
    }

    public static void putLong(@NonNull Entry<Long> entry, long value) {
        getEditor().putLong(entry.getKey(), value).apply();
    }

    public static void putFloat(@NonNull Entry<Float> entry, float value) {
        getEditor().putFloat(entry.getKey(), value).apply();
    }

    public static void putBoolean(@NonNull Entry<Boolean> entry, boolean value) {
        getEditor().putBoolean(entry.getKey(), value).apply();
    }

    public static void remove(@NonNull Entry<?> entry) {
        getEditor().remove(entry.getKey()).apply();
    }

    public static void clear() {
        getEditor().clear().apply();
    }

    public interface Entry<T> {

        @NonNull
        String getKey();

        @NonNull
        T getDefaultValue();
    }
}
