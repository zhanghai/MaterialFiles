/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.data.mediastore.MediaStoreUtil;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.common.MoreFiles;

public class PathInputStreamModelLoader implements ModelLoader<Path, InputStream> {

    @Override
    public boolean handles(@NonNull Path model) {
        return true;
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull Path model, int width, int height,
                                               @NonNull Options options) {
        if (MediaStoreUtil.isThumbnailSize(width, height) && !GlidePathUtils.shouldLoadThumbnail(
                model)) {
            return null;
        }
        return new LoadData<>(new ObjectKey(model), new Fetcher(model));
    }

    private static class Fetcher implements DataFetcher<InputStream> {

        @NonNull
        private final Path path;

        private InputStream inputStream;

        public Fetcher(@NonNull Path path) {
            this.path = path;
        }

        @Override
        public void loadData(@NonNull Priority priority,
                             @NonNull DataCallback<? super InputStream> callback) {
            try {
                inputStream = MoreFiles.newInputStream(path);
            } catch (Exception e) {
                callback.onLoadFailed(e);
                return;
            }
            callback.onDataReady(inputStream);
        }

        @Override
        public void cleanup() {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void cancel() {}

        @NonNull
        @Override
        public Class<InputStream> getDataClass() {
            return InputStream.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }

    public static class Factory implements ModelLoaderFactory<Path, InputStream> {

        @NonNull
        @Override
        public ModelLoader<Path, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new PathInputStreamModelLoader();
        }

        @Override
        public void teardown() {}
    }
}
