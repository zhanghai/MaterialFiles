/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.ArrayRes;
import androidx.annotation.BoolRes;
import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import me.zhanghai.android.files.AppApplication;
import me.zhanghai.android.files.util.LogUtils;
import me.zhanghai.android.files.util.SharedPrefsUtils;

public interface SettingsEntries {

    class StringSettingsEntry extends SettingsEntry<String> {

        public StringSettingsEntry(@StringRes int keyResId, @StringRes int defaultValueResId) {
            super(keyResId, defaultValueResId);
        }

        @NonNull
        @Override
        public String getDefaultValue() {
            return AppApplication.getInstance().getString(getDefaultValueResId());
        }

        @Nullable
        @Override
        public String getValue() {
            return SharedPrefsUtils.getString(this);
        }

        @Override
        public void putValue(@Nullable String value) {
            SharedPrefsUtils.putString(this, value);
        }
    }

    class StringSetSettingsEntry extends SettingsEntry<Set<String>> {

        public StringSetSettingsEntry(@StringRes int keyResId, @ArrayRes int defaultValueResId) {
            super(keyResId, defaultValueResId);
        }

        @NonNull
        @Override
        public Set<String> getDefaultValue() {
            Set<String> stringSet = new HashSet<>();
            Collections.addAll(stringSet, AppApplication.getInstance().getResources()
                    .getStringArray(getDefaultValueResId()));
            return stringSet;
        }

        @Nullable
        @Override
        public Set<String> getValue() {
            return SharedPrefsUtils.getStringSet(this);
        }

        @Override
        public void putValue(@Nullable Set<String> value) {
            SharedPrefsUtils.putStringSet(this, value);
        }
    }

    class IntegerSettingsEntry extends SettingsEntry<Integer> {

        public IntegerSettingsEntry(@StringRes int keyResId, @IntegerRes int defaultValueResId) {
            super(keyResId, defaultValueResId);
        }

        @NonNull
        @Override
        public Integer getDefaultValue() {
            return AppApplication.getInstance().getResources().getInteger(getDefaultValueResId());
        }

        @NonNull
        @Override
        public Integer getValue() {
            return SharedPrefsUtils.getInt(this);
        }

        @Override
        public void putValue(@NonNull Integer value) {
            SharedPrefsUtils.putInt(this, value);
        }
    }

    class LongSettingsEntry extends SettingsEntry<Long> {

        public LongSettingsEntry(@StringRes int keyResId, @StringRes int defaultValueResId) {
            super(keyResId, defaultValueResId);
        }

        @NonNull
        @Override
        public Long getDefaultValue() {
            return Long.valueOf(AppApplication.getInstance().getResources().getString(
                    getDefaultValueResId()));
        }

        @NonNull
        @Override
        public Long getValue() {
            return SharedPrefsUtils.getLong(this);
        }

        @Override
        public void putValue(@NonNull Long value) {
            SharedPrefsUtils.putLong(this, value);
        }
    }

    class FloatSettingsEntry extends SettingsEntry<Float> {

        public FloatSettingsEntry(@StringRes int keyResId, @StringRes int defaultValueResId) {
            super(keyResId, defaultValueResId);
        }

        @NonNull
        @Override
        public Float getDefaultValue() {
            return Float.valueOf(AppApplication.getInstance().getResources().getString(
                    getDefaultValueResId()));
        }

        @NonNull
        @Override
        public Float getValue() {
            return SharedPrefsUtils.getFloat(this);
        }

        @Override
        public void putValue(@NonNull Float value) {
            SharedPrefsUtils.putFloat(this, value);
        }
    }

    class BooleanSettingsEntry extends SettingsEntry<Boolean> {

        public BooleanSettingsEntry(@StringRes int keyResId, @BoolRes int defaultValueResId) {
            super(keyResId, defaultValueResId);
        }

        @NonNull
        @Override
        public Boolean getDefaultValue() {
            return AppApplication.getInstance().getResources().getBoolean(getDefaultValueResId());
        }

        @NonNull
        @Override
        public Boolean getValue() {
            return SharedPrefsUtils.getBoolean(this);
        }

        @Override
        public void putValue(@NonNull Boolean value) {
            SharedPrefsUtils.putBoolean(this, value);
        }
    }

    // Extending StringSettingsEntry so that we can support ListPreference.
    class EnumSettingsEntry<E extends Enum<E>> extends StringSettingsEntry {

        private E[] mEnumValues;

        public EnumSettingsEntry(@StringRes int keyResId, @StringRes int defaultValueResId,
                                 Class<E> enumClass) {
            super(keyResId, defaultValueResId);

            mEnumValues = enumClass.getEnumConstants();
        }

        @NonNull
        public E getDefaultEnumValue() {
            return mEnumValues[Integer.parseInt(getDefaultValue())];
        }

        @NonNull
        public E getEnumValue() {
            int ordinal = Integer.parseInt(getValue());
            if (ordinal < 0 || ordinal >= mEnumValues.length) {
                LogUtils.w("Invalid ordinal " + ordinal + ", with key=" + getKey()
                        + ", enum values=" + Arrays.toString(mEnumValues)
                        + ", reverting to default value");
                E enumValue = getDefaultEnumValue();
                putEnumValue(enumValue);
                return enumValue;
            }
            return mEnumValues[ordinal];
        }

        public void putEnumValue(@NonNull E value) {
            putValue(String.valueOf(value.ordinal()));
        }
    }

    class UriSettingsEntry extends StringSettingsEntry {

        public UriSettingsEntry(@StringRes int keyResId, @StringRes int defaultValueResId) {
            super(keyResId, defaultValueResId);
        }

        @NonNull
        public Uri getDefaultUriValue() {
            return stringToUri(getDefaultValue());
        }

        @Nullable
        public Uri getUriValue() {
            return stringToUri(getValue());
        }

        @Nullable
        private static Uri stringToUri(@Nullable String string) {
            return !TextUtils.isEmpty(string) ? Uri.parse(string) : null;
        }

        public void putUriValue(@Nullable Uri value) {
            putValue(value != null ? value.toString() : null);
        }
    }
}
