/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.jakewharton.threetenabp.AndroidThreeTen;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;
import me.zhanghai.android.files.provider.common.AndroidFileTypeDetector;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;
import me.zhanghai.android.files.util.NightModeHelper;

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

        LinuxFileSystemProvider.installAsDefault();
        ArchiveFileSystemProvider.install();
        AndroidFileTypeDetector.install();

        NightModeHelper.setup(this);
    }
}
