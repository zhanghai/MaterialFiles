/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager;

import android.app.Application;
import android.support.annotation.NonNull;

import com.facebook.stetho.Stetho;
import com.jakewharton.threetenabp.AndroidThreeTen;

import me.zhanghai.android.materialfilemanager.util.NightModeHelper;

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
        //FabricUtils.init(this);
        Stetho.initializeWithDefaults(this);

        NightModeHelper.setup(this);
    }
}
