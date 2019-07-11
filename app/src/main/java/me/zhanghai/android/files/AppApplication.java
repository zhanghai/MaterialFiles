/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.jakewharton.threetenabp.AndroidThreeTen;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.firebase.CrashlyticsUtils;
import me.zhanghai.android.files.provider.FileSystemProviders;
import me.zhanghai.android.files.theme.custom.CustomThemeHelper;
import me.zhanghai.android.files.theme.night.NightModeHelper;

public class AppApplication extends Application {

    @NonNull
    private static AppApplication sInstance;

    public AppApplication() {
        sInstance = this;
    }

    @NonNull
    public static AppApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidThreeTen.init(this);
        CrashlyticsUtils.init(this);
        Stetho.initializeWithDefaults(this);

        FileSystemProviders.install();
        FileSystemProviders.setOverflowWatchEvents(true);

        CustomThemeHelper.initialize(this);
        NightModeHelper.initialize(this);
    }
}
