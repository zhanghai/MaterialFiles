/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.facebook.stetho.Stetho;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.provider.FileSystemProviders;
import me.zhanghai.android.files.settings.Settings;
import me.zhanghai.android.files.theme.custom.CustomThemeHelper;
import me.zhanghai.android.files.theme.night.NightModeHelper;

public class AppProvider extends ContentProvider {

    @NonNull
    private static AppProvider sInstance;

    public AppProvider() {
        sInstance = this;
    }

    @NonNull
    public static Context requireContext() {
        return Objects.requireNonNull(sInstance.getContext());
    }

    @Override
    public boolean onCreate() {

        Context context = requireContext();
//#ifdef NONFREE
        me.zhanghai.android.files.nonfree.CrashlyticsUtils.init(context);
//#endif
        AndroidThreeTen.init(context);
        Stetho.initializeWithDefaults(context);

        FileSystemProviders.install();
        FileSystemProviders.setOverflowWatchEvents(true);

        // Force initialization of Settings so that it won't happen on a background thread.
        Settings.FILE_LIST_DEFAULT_DIRECTORY.getValue();

        Application application = (Application) context;
        CustomThemeHelper.initialize(application);
        NightModeHelper.initialize(application);

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
