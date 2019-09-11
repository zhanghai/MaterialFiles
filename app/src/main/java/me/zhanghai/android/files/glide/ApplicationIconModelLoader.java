/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.files.compat.ApplicationInfoCompat;

public class ApplicationIconModelLoader implements ModelLoader<ApplicationInfo, Drawable> {

    @NonNull
    private final Context context;

    public ApplicationIconModelLoader(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public boolean handles(@NonNull ApplicationInfo model) {
        return true;
    }

    @Nullable
    @Override
    public LoadData<Drawable> buildLoadData(@NonNull ApplicationInfo model, int width, int height,
                                            @NonNull Options options) {
        String key = model.packageName + ":" + ApplicationInfoCompat.getLongVersionCode(model);
        return new LoadData<>(new ObjectKey(key), new Fetcher(model, context));
    }

    private static class Fetcher implements DataFetcher<Drawable> {

        @NonNull
        private final ApplicationInfo applicationInfo;
        @NonNull
        private final Context context;

        public Fetcher(@NonNull ApplicationInfo applicationInfo, @NonNull Context context) {
            this.applicationInfo = applicationInfo;
            this.context = context.getApplicationContext();
        }

        @Override
        public void loadData(@NonNull Priority priority,
                             @NonNull DataCallback<? super Drawable> callback) {
            PackageManager packageManager = context.getPackageManager();
            Drawable icon = packageManager.getApplicationIcon(applicationInfo);
            // TODO: Add shadow for adaptive icons.
            callback.onDataReady(icon);
        }

        @Override
        public void cleanup() {}

        @Override
        public void cancel() {}

        @NonNull
        @Override
        public Class<Drawable> getDataClass() {
            return Drawable.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }

    public static class Factory implements ModelLoaderFactory<ApplicationInfo, Drawable> {

        @NonNull
        private final Context context;

        public Factory(@NonNull Context context) {
            this.context = context.getApplicationContext();
        }

        @NonNull
        @Override
        public ModelLoader<ApplicationInfo, Drawable> build(
                @NonNull MultiModelLoaderFactory multiFactory) {
            return new ApplicationIconModelLoader(context);
        }

        @Override
        public void teardown() {}
    }
}
