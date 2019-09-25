/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BundleUtils {

    @NonNull
    private static final ClassLoader sClassLoader = BundleUtils.class.getClassLoader();

    private BundleUtils() {}

    @Nullable
    public static <T extends Parcelable> T getParcelable(@NonNull Bundle bundle,
                                                         @Nullable String key) {
        return ensureClassLoader(bundle).getParcelable(key);
    }

    @Nullable
    public static Parcelable[] getParcelableArray(@NonNull Bundle bundle, @Nullable String key) {
        return ensureClassLoader(bundle).getParcelableArray(key);
    }

    @Nullable
    public static <T extends Parcelable> ArrayList<T> getParcelableArrayList(@NonNull Bundle bundle,
                                                                             @Nullable String key) {
        return ensureClassLoader(bundle).getParcelableArrayList(key);
    }

    @Nullable
    public static <T extends Parcelable> SparseArray<T> getSparseParcelableArray(
            @NonNull Bundle bundle, @Nullable String key) {
        return ensureClassLoader(bundle).getSparseParcelableArray(key);
    }

    @NonNull
    private static Bundle ensureClassLoader(@NonNull Bundle bundle) {
        bundle.setClassLoader(sClassLoader);
        return bundle;
    }

    @Nullable
    public static <T extends Parcelable> T getParcelableExtra(@NonNull Intent intent,
                                                              @Nullable String key) {
        return ensureClassLoader(intent).getParcelableExtra(key);
    }

    @Nullable
    public static Parcelable[] getParcelableArrayExtra(@NonNull Intent intent,
                                                       @Nullable String key) {
        return ensureClassLoader(intent).getParcelableArrayExtra(key);
    }

    @Nullable
    public static <T extends Parcelable> ArrayList<T> getParcelableArrayListExtra(
            @NonNull Intent intent, @Nullable String key) {
        return ensureClassLoader(intent).getParcelableArrayListExtra(key);
    }

    @NonNull
    private static Intent ensureClassLoader(@NonNull Intent intent) {
        intent.setExtrasClassLoader(sClassLoader);
        return intent;
    }
}
