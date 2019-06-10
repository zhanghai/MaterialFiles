/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.AnyRes;
import androidx.annotation.ArrayRes;
import androidx.annotation.BoolRes;
import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import me.zhanghai.android.files.AppApplication;
import me.zhanghai.android.files.R;
import me.zhanghai.android.files.util.IoUtils;
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
                putValue(null);
                return getDefaultEnumValue();
            }
            return mEnumValues[ordinal];
        }

        public void putEnumValue(@NonNull E value) {
            putValue(String.valueOf(value.ordinal()));
        }
    }

    class ResourceIdSettingsEntry extends StringSettingsEntry {

        public ResourceIdSettingsEntry(@StringRes int keyResId, @StringRes int defaultValueResId) {
            super(keyResId, defaultValueResId);
        }

        @AnyRes
        public int getDefaultResourceIdValue() {
            Context context = AppApplication.getInstance();
            return context.getResources().getIdentifier(getDefaultValue(), null,
                    context.getPackageName());
        }

        @AnyRes
        public int getResourceIdValue() {
            Context context = AppApplication.getInstance();
            int resourceId = context.getResources().getIdentifier(getValue(), null,
                    context.getPackageName());
            if (resourceId == 0) {
                LogUtils.w("Invalid resource ID " + resourceId + ", with key=" + getKey()
                        + ", string value=" + getValue() + ", reverting to default value");
                putValue(null);
                return getDefaultResourceIdValue();
            }
            return resourceId;
        }

        public void putResourceIdValue(@AnyRes int value) {
            putValue(AppApplication.getInstance().getResources().getResourceName(value));
        }
    }

    class UriSettingsEntry extends StringSettingsEntry {

        public UriSettingsEntry(@StringRes int keyResId, @StringRes int defaultValueResId) {
            super(keyResId, defaultValueResId);
        }

        @Nullable
        public Uri getDefaultUriValue() {
            return stringToUri(getDefaultValue());
        }

        @Nullable
        public Uri getUriValue() {
            return stringToUri(getValue());
        }

        public void putUriValue(@Nullable Uri value) {
            putValue(uriToString(value));
        }

        @Nullable
        private static Uri stringToUri(@Nullable String string) {
            return !TextUtils.isEmpty(string) ? Uri.parse(string) : null;
        }

        @Nullable
        private static String uriToString(@Nullable Uri uri) {
            return uri != null ? uri.toString() : null;
        }
    }

    class ParcelableSettingsEntry<T extends Parcelable> extends StringSettingsEntry {

        @Nullable
        private final ClassLoader mClassLoader;

        public ParcelableSettingsEntry(@StringRes int keyResId, @NonNull Class<T> valueClass) {
            super(keyResId, R.string.pref_default_value_empty_string);

            mClassLoader = valueClass.getClassLoader();
        }

        @Nullable
        public T getParcelableValue() {
            return base64ToParcelable(getValue(), mClassLoader);
        }

        public void putParcelableValue(@Nullable T value) {
            putValue(parcelableToBase64(value));
        }

        @Nullable
        private static <T extends Parcelable> T base64ToParcelable(
                @Nullable String base64, @Nullable ClassLoader classLoader) {
            if (TextUtils.isEmpty(base64)) {
                return null;
            }
            byte[] bytes= IoUtils.base64ToByteArray(base64);
            Parcel parcel = Parcel.obtain();
            try {
                parcel.unmarshall(bytes, 0, bytes.length);
                parcel.setDataPosition(0);
                return parcel.readParcelable(classLoader);
            } finally {
                parcel.recycle();
            }
        }

        @Nullable
        private static <T extends Parcelable> String parcelableToBase64(
                @Nullable T parcelable) {
            if (parcelable == null) {
                return null;
            }
            Parcel parcel = Parcel.obtain();
            byte[] bytes;
            try {
                parcel.writeParcelable(parcelable, 0);
                bytes = parcel.marshall();
            } finally {
                parcel.recycle();
            }
            return IoUtils.byteArrayToBase64(bytes);
        }
    }

    class TypedListSettingsEntry<T extends Parcelable> extends StringSettingsEntry {

        @NonNull
        private final Parcelable.Creator<T> mCreator;

        public TypedListSettingsEntry(@StringRes int keyResId,
                                      @NonNull Parcelable.Creator<T> creator) {
            super(keyResId, R.string.pref_default_value_empty_string);

            mCreator = creator;
        }

        @Nullable
        public List<T> getTypedListValue() {
            return base64ToTypedList(getValue(), mCreator);
        }

        public void putTypedListValue(@Nullable List<T> value) {
            putValue(typedListToBase64(value));
        }

        @Nullable
        private static <T extends Parcelable> List<T> base64ToTypedList(
                @Nullable String base64, @NonNull Parcelable.Creator<T> creator) {
            if (TextUtils.isEmpty(base64)) {
                return null;
            }
            byte[] bytes= IoUtils.base64ToByteArray(base64);
            Parcel parcel = Parcel.obtain();
            try {
                parcel.unmarshall(bytes, 0, bytes.length);
                parcel.setDataPosition(0);
                return parcel.createTypedArrayList(creator);
            } finally {
                parcel.recycle();
            }
        }

        @Nullable
        private static <T extends Parcelable> String typedListToBase64(@Nullable List<T> list) {
            if (list == null) {
                return null;
            }
            Parcel parcel = Parcel.obtain();
            byte[] bytes;
            try {
                parcel.writeTypedList(list);
                bytes = parcel.marshall();
            } finally {
                parcel.recycle();
            }
            return IoUtils.byteArrayToBase64(bytes);
        }
    }
}
