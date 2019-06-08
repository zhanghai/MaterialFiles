/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.SharedPreferences;
import android.os.Parcelable;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import me.zhanghai.android.files.util.SharedPrefsUtils;

public class TypedListSettingsLiveData<T extends Parcelable> extends LiveData<List<T>>
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @NonNull
    private final SettingsEntries.TypedListSettingsEntry<T> mEntry;

    public TypedListSettingsLiveData(@NonNull SettingsEntries.TypedListSettingsEntry<T> entry) {
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
        setValue(mEntry.getTypedListValue());
    }

    public void putValue(@NonNull List<T> value) {
        mEntry.putTypedListValue(value);
    }
}
