/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;
import me.zhanghai.android.files.AppProvider;
import me.zhanghai.android.files.compat.PreferenceManagerCompat;

public abstract class SettingLiveData<T> extends LiveData<T>
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @NonNull
    private final SharedPreferences mSharedPreferences;
    @NonNull
    private final String mKey;
    @AnyRes
    private final int mDefaultValueRes;
    private T mDefaultValue;

    public SettingLiveData(@Nullable String nameSuffix, @StringRes int keyRes,
                           @Nullable String keySuffix, @AnyRes int defaultValueRes) {
        mSharedPreferences = getSharedPreferences(nameSuffix);
        mKey = getKey(keyRes, keySuffix);
        mDefaultValueRes = defaultValueRes;
    }

    public SettingLiveData(@StringRes int keyRes, @AnyRes int defaultValueRes) {
        this(null, keyRes, null, defaultValueRes);
    }

    protected void init() {
        mDefaultValue = getDefaultValue(mDefaultValueRes);
        loadValue();
        // Only a weak reference is stored so we don't need to worry about unregistering.
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @NonNull
    private static SharedPreferences getSharedPreferences(@Nullable String nameSuffix) {
        Context context = AppProvider.requireContext();
        if (nameSuffix == null) {
            return PreferenceManager.getDefaultSharedPreferences(context);
        } else {
            String name = PreferenceManagerCompat.getDefaultSharedPreferencesName(context) + '_'
                    + nameSuffix;
            int mode = PreferenceManagerCompat.getDefaultSharedPreferencesMode();
            return context.getSharedPreferences(name, mode);
        }
    }

    @NonNull
    private static String getKey(@StringRes int keyRes, @Nullable String keySuffix) {
        String key = AppProvider.requireContext().getString(keyRes);
        if (keySuffix != null) {
            key = key + '_' + keySuffix;
        }
        return key;
    }

    protected abstract T getDefaultValue(@AnyRes int defaultValueRes);

    private void loadValue() {
        setValue(getValue(mSharedPreferences, mKey, mDefaultValue));
    }

    @Override
    public void onSharedPreferenceChanged(@NonNull SharedPreferences sharedPreferences,
                                          @NonNull String key) {
        if (Objects.equals(key, mKey)) {
            loadValue();
        }
    }

    protected abstract T getValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                                  T defaultValue);

    public final void putValue(T value) {
        putValue(mSharedPreferences, mKey, value);
    }

    protected abstract void putValue(@NonNull SharedPreferences sharedPreferences,
                                     @NonNull String key, T value);
}
