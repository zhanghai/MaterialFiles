/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.firebase;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;

import io.fabric.sdk.android.Fabric;
import me.zhanghai.android.files.BuildConfig;

public class CrashlyticsUtils {

    private CrashlyticsUtils() {}

    public static void init(Context context) {
        if (BuildConfig.DEBUG) {
            return;
        }
        Fabric.with(context, new Crashlytics(), new CrashlyticsNdk());
    }
}
