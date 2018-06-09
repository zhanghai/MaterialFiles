/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

import me.zhanghai.android.materialfilemanager.AppApplication;

public class SharedPrefsUtils {

    private SharedPrefsUtils() {}

    public static SharedPreferences getSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(AppApplication.getInstance());
    }

    public static String getString(Entry<String> entry) {
        return getSharedPrefs().getString(entry.getKey(), entry.getDefaultValue());
    }

    public static Set<String> getStringSet(Entry<Set<String>> entry) {
        return getSharedPrefs().getStringSet(entry.getKey(), entry.getDefaultValue());
    }

    public static int getInt(Entry<Integer> entry) {
        return getSharedPrefs().getInt(entry.getKey(), entry.getDefaultValue());
    }

    public static long getLong(Entry<Long> entry) {
        return getSharedPrefs().getLong(entry.getKey(), entry.getDefaultValue());
    }

    public static float getFloat(Entry<Float> entry) {
        return getSharedPrefs().getFloat(entry.getKey(), entry.getDefaultValue());
    }

    public static boolean getBoolean(Entry<Boolean> entry) {
        return getSharedPrefs().getBoolean(entry.getKey(), entry.getDefaultValue());
    }

    public static SharedPreferences.Editor getEditor() {
        return getSharedPrefs().edit();
    }

    public static void putString(Entry<String> entry, String value) {
        getEditor().putString(entry.getKey(), value).apply();
    }

    public static void putStringSet(Entry<Set<String>> entry, Set<String> value) {
        getEditor().putStringSet(entry.getKey(), value).apply();
    }

    public static void putInt(Entry<Integer> entry, int value) {
        getEditor().putInt(entry.getKey(), value).apply();
    }

    public static void putLong(Entry<Long> entry, long value) {
        getEditor().putLong(entry.getKey(), value).apply();
    }

    public static void putFloat(Entry<Float> entry, float value) {
        getEditor().putFloat(entry.getKey(), value).apply();
    }

    public static void putBoolean(Entry<Boolean> entry, boolean value) {
        getEditor().putBoolean(entry.getKey(), value).apply();
    }

    public static void remove(Entry<?> entry) {
        getEditor().remove(entry.getKey()).apply();
    }

    public static void clear() {
        getEditor().clear().apply();
    }

    public interface Entry<T> {
        String getKey();
        T getDefaultValue();
    }
}
