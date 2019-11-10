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
import me.zhanghai.android.files.AppProvider;
import me.zhanghai.android.files.util.IoUtils;

interface SettingLiveDatas {

    class StringSettingLiveData extends SettingLiveData<String> {

        public StringSettingLiveData(@Nullable String nameSuffix, @StringRes int keyRes,
                                     @Nullable String keySuffix, @StringRes int defaultValueRes) {
            super(nameSuffix, keyRes, keySuffix, defaultValueRes);

            init();
        }

        public StringSettingLiveData(@StringRes int keyRes, @StringRes int defaultValueRes) {
            this(null, keyRes, null, defaultValueRes);
        }

        @NonNull
        @Override
        protected String getDefaultValue(@StringRes int defaultValueRes) {
            return AppProvider.requireContext().getString(defaultValueRes);
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

        public StringSetSettingLiveData(@Nullable String nameSuffix, @StringRes int keyRes,
                                        @Nullable String keySuffix, @ArrayRes int defaultValueRes) {
            super(nameSuffix, keyRes, keySuffix, defaultValueRes);

            init();
        }

        public StringSetSettingLiveData(@StringRes int keyRes, @ArrayRes int defaultValueRes) {
            this(null, keyRes, null, defaultValueRes);
        }

        @NonNull
        @Override
        protected Set<String> getDefaultValue(@StringRes int defaultValueRes) {
            String[] defaultValueArray = AppProvider.requireContext().getResources().getStringArray(
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

        public IntegerSettingLiveData(@Nullable String nameSuffix, @StringRes int keyRes,
                                      @Nullable String keySuffix, @IntegerRes int defaultValueRes) {
            super(nameSuffix, keyRes, keySuffix, defaultValueRes);

            init();
        }

        public IntegerSettingLiveData(@StringRes int keyRes, @IntegerRes int defaultValueRes) {
            this(null, keyRes, null, defaultValueRes);
        }

        @NonNull
        @Override
        protected Integer getDefaultValue(@IntegerRes int defaultValueRes) {
            return AppProvider.requireContext().getResources().getInteger(defaultValueRes);
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

        public LongSettingLiveData(@Nullable String nameSuffix, @StringRes int keyRes,
                                   @Nullable String keySuffix, @StringRes int defaultValueRes) {
            super(nameSuffix, keyRes, keySuffix, defaultValueRes);

            init();
        }

        public LongSettingLiveData(@StringRes int keyRes, @StringRes int defaultValueRes) {
            this(null, keyRes, null, defaultValueRes);
        }

        @NonNull
        @Override
        protected Long getDefaultValue(@StringRes int defaultValueRes) {
            String defaultValueString = AppProvider.requireContext().getResources().getString(
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

        public FloatSettingLiveData(@Nullable String nameSuffix, @StringRes int keyRes,
                                    @Nullable String keySuffix, @DimenRes int defaultValueRes) {
            super(nameSuffix, keyRes, keySuffix, defaultValueRes);

            init();
        }

        public FloatSettingLiveData(@StringRes int keyRes, @DimenRes int defaultValueRes) {
            this(null, keyRes, null, defaultValueRes);
        }

        @NonNull
        @Override
        protected Float getDefaultValue(@DimenRes int defaultValueRes) {
            return ResourcesCompat.getFloat(AppProvider.requireContext().getResources(),
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

        public BooleanSettingLiveData(@Nullable String nameSuffix, @StringRes int keyRes,
                                      @Nullable String keySuffix, @BoolRes int defaultValueRes) {
            super(nameSuffix, keyRes, keySuffix, defaultValueRes);

            init();
        }

        public BooleanSettingLiveData(@StringRes int keyRes, @BoolRes int defaultValueRes) {
            this(null, keyRes, null, defaultValueRes);
        }

        @NonNull
        @Override
        protected Boolean getDefaultValue(@BoolRes int defaultValueRes) {
            return AppProvider.requireContext().getResources().getBoolean(defaultValueRes);
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

        public EnumSettingLiveData(@Nullable String nameSuffix, @StringRes int keyRes,
                                   @Nullable String keySuffix, @StringRes int defaultValueRes,
                                   @NonNull Class<E> enumClass) {
            super(nameSuffix, keyRes, keySuffix, defaultValueRes);

            mEnumValues = enumClass.getEnumConstants();
            init();
        }

        public EnumSettingLiveData(@StringRes int keyRes, @StringRes int defaultValueRes,
                                   @NonNull Class<E> enumClass) {
            this(null, keyRes, null, defaultValueRes, enumClass);
        }

        @NonNull
        @Override
        protected E getDefaultValue(@StringRes int defaultValueRes) {
            String defaultValueString = AppProvider.requireContext().getString(defaultValueRes);
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
            int valueOrdinal;
            try {
                valueOrdinal = Integer.parseInt(valueString);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                valueOrdinal = -1;
            }
            if (valueOrdinal < 0 || valueOrdinal >= mEnumValues.length) {
                return defaultValue;
            }
            return mEnumValues[valueOrdinal];
        }

        @Override
        public void putValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                             @Nullable E value) {
            String valueString = value != null ? Integer.toString(value.ordinal()) : null;
            sharedPreferences.edit().putString(key, valueString).apply();
        }
    }

    class ResourceIdSettingLiveData extends SettingLiveData<Integer> {

        public ResourceIdSettingLiveData(@Nullable String nameSuffix, @StringRes int keyRes,
                                         @Nullable String keySuffix, @AnyRes int defaultValue) {
            super(nameSuffix, keyRes, keySuffix, defaultValue);

            init();
        }

        public ResourceIdSettingLiveData(@StringRes int keyRes, @AnyRes int defaultValue) {
            this(null, keyRes, null, defaultValue);
        }

        @AnyRes
        @NonNull
        @Override
        protected Integer getDefaultValue(@AnyRes int defaultValueRes) {
            return defaultValueRes;
        }

        @NonNull
        @Override
        public Integer getValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                                @AnyRes @NonNull Integer defaultValue) {
            String valueString = sharedPreferences.getString(key, null);
            if (valueString == null) {
                return defaultValue;
            }
            Context context = AppProvider.requireContext();
            int value = context.getResources().getIdentifier(valueString, null,
                    context.getPackageName());
            if (value == 0) {
                return defaultValue;
            }
            return value;
        }

        @Override
        public void putValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                             @AnyRes @NonNull Integer value) {
            String valueString = AppProvider.requireContext().getResources().getResourceName(value);
            sharedPreferences.edit().putString(key, valueString).apply();
        }
    }

    class ParcelableSettingLiveData<T extends Parcelable> extends SettingLiveData<T> {

        private final T mDefaultValue;
        @Nullable
        private final ClassLoader mClassLoader;

        public ParcelableSettingLiveData(@Nullable String nameSuffix, @StringRes int keyRes,
                                         @Nullable String keySuffix, T defaultValue,
                                         @NonNull Class<T> class_) {
            super(nameSuffix, keyRes, keySuffix, 0);

            mDefaultValue = defaultValue;
            mClassLoader = class_.getClassLoader();
            init();
        }

        public ParcelableSettingLiveData(@StringRes int keyRes, @Nullable T defaultValue,
                                         @NonNull Class<T> class_) {
            this(null, keyRes, null, defaultValue, class_);
        }

        @Override
        protected T getDefaultValue(@AnyRes int defaultValueRes) {
            return mDefaultValue;
        }

        @Override
        public T getValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                          T defaultValue) {
            String valueString = sharedPreferences.getString(key, null);
            T value;
            try {
                value = base64ToParcelable(valueString, mClassLoader);
            } catch (Exception e) {
                e.printStackTrace();
                value = null;
            }
            if (value == null) {
                return mDefaultValue;
            }
            return value;
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
            byte[] bytes = IoUtils.base64ToByteArray(base64);
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

        private final List<T> mDefaultValue;
        @NonNull
        private final Parcelable.Creator<T> mCreator;

        public ParcelableListSettingLiveData(@Nullable String nameSuffix, @StringRes int keyRes,
                                             @Nullable String keySuffix, List<T> defaultValue,
                                             @NonNull Parcelable.Creator<T> creator) {
            super(nameSuffix, keyRes, keySuffix, 0);

            mDefaultValue = defaultValue;
            mCreator = creator;
            init();
        }

        public ParcelableListSettingLiveData(@StringRes int keyRes, List<T> defaultValue,
                                             @NonNull Parcelable.Creator<T> creator) {
            this(null, keyRes, null, defaultValue, creator);
        }

        @Override
        protected List<T> getDefaultValue(@AnyRes int defaultValueRes) {
            return mDefaultValue;
        }

        @Nullable
        @Override
        public List<T> getValue(@NonNull SharedPreferences sharedPreferences, @NonNull String key,
                                List<T> defaultValue) {
            String valueString = sharedPreferences.getString(key, null);
            List<T> value;
            try {
                value = base64ToTypedList(valueString, mCreator);
            } catch (Exception e) {
                e.printStackTrace();
                value = null;
            }
            if (value == null) {
                return defaultValue;
            }
            return value;
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
            byte[] bytes = IoUtils.base64ToByteArray(base64);
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
