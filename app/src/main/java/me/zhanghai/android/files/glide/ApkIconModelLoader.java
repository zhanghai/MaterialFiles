/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide;

import android.content.Context;
import android.content.pm.PackageInfo;
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

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;

public class ApkIconModelLoader implements ModelLoader<Path, Drawable> {

    @NonNull
    private final Context context;

    public ApkIconModelLoader(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public boolean handles(@NonNull Path model) {
        if (!LinuxFileSystemProvider.isLinuxPath(model)) {
            return false;
        }
        String mimeType = MimeTypes.getMimeType(model.toFile().getPath());
        return Objects.equals(mimeType, "application/vnd.android.package-archive");
    }

    @Nullable
    @Override
    public LoadData<Drawable> buildLoadData(@NonNull Path model, int width, int height,
                                            @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model), new Fetcher(model.toFile().getPath(), context));
    }

    private static class Fetcher implements DataFetcher<Drawable> {

        @NonNull
        private final String path;
        @NonNull
        private final Context context;

        public Fetcher(@NonNull String path, @NonNull Context context) {
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
                        "PackageManager.getPackageArchiveInfo() returned null: " + path));
                return;
            }
            packageInfo.applicationInfo.sourceDir = path;
            packageInfo.applicationInfo.publicSourceDir = path;
            Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
            if (icon == null) {
                callback.onLoadFailed(new NullPointerException(
                        "ApplicationInfo.loadIcon() returned null: " + path));
                return;
            }
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

    public static class Factory implements ModelLoaderFactory<Path, Drawable> {

        @NonNull
        private final Context context;

        public Factory(@NonNull Context context) {
            this.context = context.getApplicationContext();
        }

        @NonNull
        @Override
        public ModelLoader<Path, Drawable> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new ApkIconModelLoader(context);
        }

        @Override
        public void teardown() {}
    }
}
