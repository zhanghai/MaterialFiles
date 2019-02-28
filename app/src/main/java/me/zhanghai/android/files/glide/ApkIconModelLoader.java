/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;

public class ApkIconModelLoader<Model> implements ModelLoader<Model, Drawable> {

    @NonNull
    private final Context context;

    public ApkIconModelLoader(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public boolean handles(@NonNull Model model) {
        String path = getPath(model);
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        String mimeType = MimeTypes.getMimeType(path);
        return TextUtils.equals(mimeType, "application/vnd.android.package-archive");
    }

    @Nullable
    @Override
    public LoadData<Drawable> buildLoadData(@NonNull Model model, int width, int height,
                                            @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model), new DataFetcher(getPath(model), context));
    }

    @NonNull
    private String getPath(@NonNull Model model) {
        if (model instanceof String) {
            return (String) model;
        } else if (model instanceof File) {
            File file = (File) model;
            return file.getPath();
        } else if (model instanceof Path) {
            Path path = (Path) model;
            if (LinuxFileSystemProvider.isLinuxPath(path)) {
                return path.toFile().getPath();
            }
        }
        throw new IllegalArgumentException("Unable to get path from model: " + model);
    }

    private static class DataFetcher implements com.bumptech.glide.load.data.DataFetcher<Drawable> {

        @NonNull
        private final String path;
        @NonNull
        private final Context context;

        public DataFetcher(@NonNull String path, @NonNull Context context) {
            this.path = path;
            this.context = context.getApplicationContext();
        }

        @Override
        public void loadData(@NonNull Priority priority,
                             @NonNull DataCallback<? super Drawable> callback) {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageArchiveInfo(path, 0);
            if (packageInfo == null) {
                callback.onLoadFailed(new NullPointerException(
                        "PackageManager.getPackageArchiveInfo() returned null"));
                return;
            }
            packageInfo.applicationInfo.sourceDir = path;
            packageInfo.applicationInfo.publicSourceDir = path;
            Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
            if (icon == null) {
                callback.onLoadFailed(new NullPointerException(
                        "ApplicationInfo.loadIcon() returned null"));
                return;
            }
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

    public static class Factory<Model> implements ModelLoaderFactory<Model, Drawable> {

        @NonNull
        private final Context context;

        public Factory(@NonNull Context context) {
            this.context = context.getApplicationContext();
        }

        @NonNull
        @Override
        public ModelLoader<Model, Drawable> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new ApkIconModelLoader<>(context);
        }

        @Override
        public void teardown() {}
    }
}
