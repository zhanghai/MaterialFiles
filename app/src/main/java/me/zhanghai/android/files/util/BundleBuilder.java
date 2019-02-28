/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.util.Size;
import android.util.SizeF;
import android.util.SparseArray;

import java.io.Serializable;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.BundleCompat;

@SuppressWarnings("unused")
public class BundleBuilder {

    @NonNull
    private Bundle mBundle;

    private BundleBuilder(@NonNull Bundle bundle) {
        mBundle = bundle;
    }

    public BundleBuilder() {
        this(new Bundle());
    }

    @NonNull
    public static BundleBuilder buildUpon(@NonNull Bundle bundle) {
        return new BundleBuilder(bundle);
    }

    @NonNull
    public Bundle build() {
        Bundle bundle = mBundle;
        mBundle = null;
        return bundle;
    }


    @NonNull
    public BundleBuilder setClassLoader(@NonNull ClassLoader loader) {
        mBundle.setClassLoader(loader);
        return this;
    }

    @NonNull
    public BundleBuilder clear() {
        mBundle.clear();
        return this;
    }

    @NonNull
    public BundleBuilder remove(@Nullable String key) {
        mBundle.remove(key);
        return this;
    }

    @NonNull
    public BundleBuilder putAll(@NonNull Bundle bundle) {
        mBundle.putAll(bundle);
        return this;
    }

    @NonNull
    public BundleBuilder putByte(@Nullable String key, byte value) {
        mBundle.putByte(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putChar(@Nullable String key, char value) {
        mBundle.putChar(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putShort(@Nullable String key, short value) {
        mBundle.putShort(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putFloat(@Nullable String key, float value) {
        mBundle.putFloat(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putCharSequence(@Nullable String key, @Nullable CharSequence value) {
        mBundle.putCharSequence(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putParcelable(@Nullable String key, @Nullable Parcelable value) {
        mBundle.putParcelable(key, value);
        return this;
    }

    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public BundleBuilder putSize(@Nullable String key, @Nullable Size value) {
        mBundle.putSize(key, value);
        return this;
    }

    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public BundleBuilder putSizeF(@Nullable String key, @Nullable SizeF value) {
        mBundle.putSizeF(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putParcelableArray(@Nullable String key, @Nullable Parcelable[] value) {
        mBundle.putParcelableArray(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putParcelableArrayList(@Nullable String key,
                                                @Nullable ArrayList<? extends Parcelable> value) {
        mBundle.putParcelableArrayList(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putSparseParcelableArray(
            @Nullable String key, @Nullable SparseArray<? extends Parcelable> value) {
        mBundle.putSparseParcelableArray(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putIntegerArrayList(@Nullable String key,
                                             @Nullable ArrayList<Integer> value) {
        mBundle.putIntegerArrayList(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putStringArrayList(@Nullable String key,
                                            @Nullable ArrayList<String> value) {
        mBundle.putStringArrayList(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putCharSequenceArrayList(@Nullable String key,
                                                  @Nullable ArrayList<CharSequence> value) {
        mBundle.putCharSequenceArrayList(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putSerializable(@Nullable String key, @Nullable Serializable value) {
        mBundle.putSerializable(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putByteArray(@Nullable String key, @Nullable byte[] value) {
        mBundle.putByteArray(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putShortArray(@Nullable String key, @Nullable short[] value) {
        mBundle.putShortArray(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putCharArray(@Nullable String key, @Nullable char[] value) {
        mBundle.putCharArray(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putFloatArray(@Nullable String key, @Nullable float[] value) {
        mBundle.putFloatArray(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putCharSequenceArray(@Nullable String key,
                                              @Nullable CharSequence[] value) {
        mBundle.putCharSequenceArray(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putBundle(@Nullable String key, @Nullable Bundle value) {
        mBundle.putBundle(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putBinder(@Nullable String key, @Nullable IBinder value) {
        BundleCompat.putBinder(mBundle, key, value);
        return this;
    }

    @NonNull
    public BundleBuilder writeToParcel(@NonNull Parcel parcel, int flags) {
        mBundle.writeToParcel(parcel, flags);
        return this;
    }

    @NonNull
    public BundleBuilder readFromParcel(@NonNull Parcel parcel) {
        mBundle.readFromParcel(parcel);
        return this;
    }

    @NonNull
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public BundleBuilder putAll(@NonNull PersistableBundle bundle) {
        mBundle.putAll(bundle);
        return this;
    }

    @NonNull
    public BundleBuilder putBoolean(@Nullable String key, boolean value) {
        mBundle.putBoolean(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putInt(@Nullable String key, int value) {
        mBundle.putInt(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putLong(@Nullable String key, long value) {
        mBundle.putLong(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putDouble(@Nullable String key, double value) {
        mBundle.putDouble(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putString(@Nullable String key, @Nullable String value) {
        mBundle.putString(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putBooleanArray(@Nullable String key, @Nullable boolean[] value) {
        mBundle.putBooleanArray(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putIntArray(@Nullable String key, @Nullable int[] value) {
        mBundle.putIntArray(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putLongArray(@Nullable String key, @Nullable long[] value) {
        mBundle.putLongArray(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putDoubleArray(@Nullable String key, @Nullable double[] value) {
        mBundle.putDoubleArray(key, value);
        return this;
    }

    @NonNull
    public BundleBuilder putStringArray(@Nullable String key, @Nullable String[] value) {
        mBundle.putStringArray(key, value);
        return this;
    }
}
