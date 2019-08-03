/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.AnyRes;
import androidx.annotation.ArrayRes;
import androidx.annotation.BoolRes;
import androidx.annotation.DimenRes;
import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.res.ResourcesCompat;
import me.zhanghai.android.files.AppApplication;
import me.zhanghai.android.files.util.IoUtils;

interface SettingLiveDatas {

    class StringSettingLiveData extends SettingLiveData<String> {

        public StringSettingLiveData(@Nullable String name, @NonNull String key,
                                     @StringRes int defaultValueRes) {
            super(name, key, defaultValueRes);
        }

        public StringSettingLiveData(@StringRes int keyRes, @StringRes int defaultValueRes) {
            super(keyRes, defaultValueRes);
        }

        @NonNull
        @Override
        protected String getDefaultValue(@StringRes int defaultValueRes) {
            return AppApplication.getInstance().getString(defaultValueRes);
        }

        @NonNull
        @Override
        public String getValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                               @NonNull String defaultValue) {
            return sharedPreferences.getString(key, defaultValue);
        }

        @Override
        public void putValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                             @Nullable String value) {
            sharedPreferences.edit().putString(key, value).apply();
        }
    }

    class StringSetSettingLiveData extends SettingLiveData<Set<String>> {

        public StringSetSettingLiveData(@Nullable String name, @NonNull String key,
                                        @ArrayRes int defaultValueRes) {
            super(name, key, defaultValueRes);
        }

        public StringSetSettingLiveData(@StringRes int keyRes, @ArrayRes int defaultValueRes) {
            super(keyRes, defaultValueRes);
        }

        @NonNull
        @Override
        protected Set<String> getDefaultValue(@StringRes int defaultValueRes) {
            String[] defaultValueArray = AppApplication.getInstance().getResources().getStringArray(
                    defaultValueRes);
            Set<String> defaultValue = new HashSet<>();
            Collections.addAll(defaultValue, defaultValueArray);
            return defaultValue;
        }

        @NonNull
        @Override
        public Set<String> getValue(@NonNull SharedPreferences sharedPreferences,
                                    @NonNull String key, @NonNull Set<String> defaultValue) {
            return sharedPreferences.getStringSet(key, defaultValue);
        }

        @Override
        public void putValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                             @Nullable Set<String> value) {
            sharedPreferences.edit().putStringSet(key, value).apply();
        }
    }

    class IntegerSettingLiveData extends SettingLiveData<Integer> {

        public IntegerSettingLiveData(@Nullable String name, @NonNull String key,
                                      @IntegerRes int defaultValueRes) {
            super(name, key, defaultValueRes);
        }

        public IntegerSettingLiveData(@StringRes int keyRes, @IntegerRes int defaultValueRes) {
            super(keyRes, defaultValueRes);
        }

        @NonNull
        @Override
        protected Integer getDefaultValue(@IntegerRes int defaultValueRes) {
            return AppApplication.getInstance().getResources().getInteger(defaultValueRes);
        }

        @NonNull
        @Override
        public Integer getValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                                @NonNull Integer defaultValue) {
            return sharedPreferences.getInt(key, defaultValue);
        }

        @Override
        public void putValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                             @NonNull Integer value) {
            sharedPreferences.edit().putInt(key, value).apply();
        }
    }

    class LongSettingLiveData extends SettingLiveData<Long> {

        public LongSettingLiveData(@Nullable String name, @NonNull String key,
                                   @StringRes int defaultValueRes) {
            super(name, key, defaultValueRes);
        }

        public LongSettingLiveData(@StringRes int keyRes, @StringRes int defaultValueRes) {
            super(keyRes, defaultValueRes);
        }

        @NonNull
        @Override
        protected Long getDefaultValue(@StringRes int defaultValueRes) {
            String defaultValueString = AppApplication.getInstance().getResources().getString(
                    defaultValueRes);
            return Long.valueOf(defaultValueString);
        }

        @NonNull
        @Override
        public Long getValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                             @NonNull Long defaultValue) {
            return sharedPreferences.getLong(key, defaultValue);
        }

        @Override
        public void putValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                             @NonNull Long value) {
            sharedPreferences.edit().putLong(key, value).apply();
        }
    }

    class FloatSettingLiveData extends SettingLiveData<Float> {

        public FloatSettingLiveData(@Nullable String name, @NonNull String key,
                                    @DimenRes int defaultValueRes) {
            super(name, key, defaultValueRes);
        }

        public FloatSettingLiveData(@StringRes int keyRes, @DimenRes int defaultValueRes) {
            super(keyRes, defaultValueRes);
        }

        @NonNull
        @Override
        protected Float getDefaultValue(@DimenRes int defaultValueRes) {
            return ResourcesCompat.getFloat(AppApplication.getInstance().getResources(),
                    defaultValueRes);
        }

        @NonNull
        @Override
        public Float getValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                              @NonNull Float defaultValue) {
            return sharedPreferences.getFloat(key, defaultValue);
        }

        @Override
        public void putValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                             @NonNull Float value) {
            sharedPreferences.edit().putFloat(key, value).apply();
        }
    }

    class BooleanSettingLiveData extends SettingLiveData<Boolean> {

        public BooleanSettingLiveData(@Nullable String name, @NonNull String key,
                                      @BoolRes int defaultValueRes) {
            super(name, key, defaultValueRes);
        }

        public BooleanSettingLiveData(@StringRes int keyRes, @BoolRes int defaultValueRes) {
            super(keyRes, defaultValueRes);
        }

        @NonNull
        @Override
        protected Boolean getDefaultValue(@BoolRes int defaultValueRes) {
            return AppApplication.getInstance().getResources().getBoolean(defaultValueRes);
        }

        @NonNull
        @Override
        public Boolean getValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                                @NonNull Boolean defaultValue) {
            return sharedPreferences.getBoolean(key, defaultValue);
        }

        @Override
        public void putValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                             @NonNull Boolean value) {
            sharedPreferences.edit().putBoolean(key, value).apply();
        }
    }

    // Use string resource for default value so that we can support ListPreference.
    class EnumSettingLiveData<E extends Enum<E>> extends SettingLiveData<E> {

        @NonNull
        private final E[] mEnumValues;

        public EnumSettingLiveData(@Nullable String name, @NonNull String key,
                                   @StringRes int defaultValueRes, @NonNull Class<E> enumClass) {
            super(name, key, defaultValueRes);

            mEnumValues = enumClass.getEnumConstants();
        }

        public EnumSettingLiveData(@StringRes int keyRes, @StringRes int defaultValueRes,
                                   @NonNull Class<E> enumClass) {
            super(keyRes, defaultValueRes);

            mEnumValues = enumClass.getEnumConstants();
        }

        @NonNull
        @Override
        protected E getDefaultValue(@StringRes int defaultValueRes) {
            String defaultValueString = AppApplication.getInstance().getString(defaultValueRes);
            return mEnumValues[Integer.parseInt(defaultValueString)];
        }

        @NonNull
        @Override
        public E getValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                          @NonNull E defaultValue) {
            String valueString = sharedPreferences.getString(key, null);
            if (valueString == null) {
                return defaultValue;
            }
            return mEnumValues[Integer.parseInt(valueString)];
        }

        @Override
        public void putValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                             @Nullable E value) {
            String valueString = value != null ? Integer.toString(value.ordinal()) : null;
            sharedPreferences.edit().putString(key, valueString).apply();
        }
    }

    class ResourceIdSettingLiveData extends SettingLiveData<Integer> {

        public ResourceIdSettingLiveData(@Nullable String name, @NonNull String key,
                                         @StringRes int defaultValueRes) {
            super(name, key, defaultValueRes);
        }

        public ResourceIdSettingLiveData(@StringRes int keyRes, @StringRes int defaultValueRes) {
            super(keyRes, defaultValueRes);
        }

        @AnyRes
        @NonNull
        @Override
        protected Integer getDefaultValue(@StringRes int defaultValueRes) {
            Context context = AppApplication.getInstance();
            String defaultValueString = context.getString(defaultValueRes);
            return context.getResources().getIdentifier(defaultValueString, null,
                    context.getPackageName());
        }

        @NonNull
        @Override
        public Integer getValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                                @AnyRes @NonNull Integer defaultValue) {
            String valueString = sharedPreferences.getString(key, null);
            if (valueString == null) {
                return defaultValue;
            }
            Context context = AppApplication.getInstance();
            return context.getResources().getIdentifier(valueString, null,
                    context.getPackageName());
        }

        @Override
        public void putValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                             @AnyRes @NonNull Integer value) {
            String valueString = AppApplication.getInstance().getResources().getResourceName(value);
            sharedPreferences.edit().putString(key, valueString).apply();
        }
    }

    class ParcelableSettingLiveData<T extends Parcelable> extends SettingLiveData<T> {

        @Nullable
        private final ClassLoader mClassLoader;

        public ParcelableSettingLiveData(@Nullable String name, @NonNull String key,
                                         @NonNull Class<T> class_) {
            super(name, key, 0);

            mClassLoader = class_.getClassLoader();
        }

        public ParcelableSettingLiveData(@StringRes int keyRes, @NonNull Class<T> parcelableClass) {
            super(keyRes, 0);

            mClassLoader = parcelableClass.getClassLoader();
        }

        @Nullable
        @Override
        protected T getDefaultValue(@AnyRes int defaultValueRes) {
            return null;
        }

        @Nullable
        @Override
        public T getValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                          @Nullable T defaultValue) {
            String valueString = sharedPreferences.getString(key, null);
            return base64ToParcelable(valueString, mClassLoader);
        }

        @Override
        public void putValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                             @Nullable T value) {
            String valueString = parcelableToBase64(value);
            sharedPreferences.edit().putString(key, valueString).apply();
        }

        @Nullable
        private static <T extends Parcelable> T base64ToParcelable(
                @Nullable String base64, @Nullable ClassLoader classLoader) {
            if (base64 == null) {
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
        private static <T extends Parcelable> String parcelableToBase64(@Nullable T parcelable) {
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

    class ParcelableListSettingLiveData<T extends Parcelable> extends SettingLiveData<List<T>> {

        @NonNull
        private final Parcelable.Creator<T> mCreator;

        public ParcelableListSettingLiveData(@Nullable String name, @NonNull String key,
                                             @NonNull Parcelable.Creator<T> creator) {
            super(name, key, 0);

            mCreator = creator;
        }

        public ParcelableListSettingLiveData(@StringRes int keyRes,
                                             @NonNull Parcelable.Creator<T> creator) {
            super(keyRes, 0);

            mCreator = creator;
        }

        @Nullable
        @Override
        protected List<T> getDefaultValue(@AnyRes int defaultValueRes) {
            return null;
        }

        @Nullable
        @Override
        public List<T> getValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                                @Nullable List<T> defaultValue) {
            String valueString = sharedPreferences.getString(key, null);
            return base64ToTypedList(valueString, mCreator);
        }

        @Override
        public void putValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                             @Nullable List<T> value) {
            String valueString = typedListToBase64(value);
            sharedPreferences.edit().putString(key, valueString).apply();
        }

        @Nullable
        private static <T extends Parcelable> List<T> base64ToTypedList(
                @Nullable String base64, @NonNull Parcelable.Creator<T> creator) {
            if (base64 == null) {
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
