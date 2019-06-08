/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.SharedPreferences;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import me.zhanghai.android.files.util.SharedPrefsUtils;

public class EnumSettingsLiveData<E extends Enum<E>> extends LiveData<E>
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @NonNull
    private final SettingsEntries.EnumSettingsEntry<E> mEntry;

    public EnumSettingsLiveData(@NonNull SettingsEntries.EnumSettingsEntry<E> entry) {
        mEntry = entry;

        loadValue();
        SharedPrefsUtils.getSharedPrefs().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(@NonNull SharedPreferences sharedPreferences,
                                          @NonNull String key) {
        if (Objects.equals(key, mEntry.getKey())) {
            loadValue();
        }
    }

    private void loadValue() {
        setValue(mEntry.getEnumValue());
    }

    public void putValue(@NonNull E value) {
        mEntry.putEnumValue(value);
    }
}
