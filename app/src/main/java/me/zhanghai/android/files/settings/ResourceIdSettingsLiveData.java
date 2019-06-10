/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.SharedPreferences;

import java.util.Objects;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import me.zhanghai.android.files.util.SharedPrefsUtils;

public class ResourceIdSettingsLiveData extends LiveData<Integer>
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @NonNull
    private final SettingsEntries.ResourceIdSettingsEntry mEntry;

    public ResourceIdSettingsLiveData(@NonNull SettingsEntries.ResourceIdSettingsEntry entry) {
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
        setValue(mEntry.getResourceIdValue());
    }

    public void putValue(@AnyRes int value) {
        mEntry.putResourceIdValue(value);
    }
}
