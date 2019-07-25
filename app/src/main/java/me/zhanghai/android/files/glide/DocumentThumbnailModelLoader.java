/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide;

import android.graphics.Bitmap;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.data.mediastore.MediaStoreUtil;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.content.resolver.ResolverException;
import me.zhanghai.android.files.provider.document.DocumentFileSystemProvider;
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver;

public class DocumentThumbnailModelLoader implements ModelLoader<Path, Bitmap> {

    @Override
    public boolean handles(@NonNull Path model) {
        return DocumentFileSystemProvider.isDocumentPath(model);
    }

    @Nullable
    @Override
    public LoadData<Bitmap> buildLoadData(@NonNull Path model, int width, int height,
                                          @NonNull Options options) {
        // @see MediaStoreImageThumbLoader#buildLoadData(Uri, int, int, Options)
        if (!MediaStoreUtil.isThumbnailSize(width, height)) {
            return null;
        }
        return new LoadData<>(new ObjectKey(model), new Fetcher((DocumentResolver.Path) model,
                width, height));
    }

    private static class Fetcher implements DataFetcher<Bitmap> {

        @NonNull
        private final DocumentResolver.Path path;
        private final int width;
        private final int height;

        public Fetcher(@NonNull DocumentResolver.Path path, int width, int height) {
            this.path = path;
            this.width = width;
            this.height = height;
        }

        @Override
        public void loadData(@NonNull Priority priority,
                             @NonNull DataCallback<? super Bitmap> callback) {
            Bitmap thumbnail;
            try {
                thumbnail = DocumentResolver.getThumbnail(path, width, height);
            } catch (ResolverException e) {
                callback.onLoadFailed(e);
                return;
            }
            callback.onDataReady(thumbnail);
        }

        @Override
        public void cleanup() {}

        @Override
        public void cancel() {}

        @NonNull
        @Override
        public Class<Bitmap> getDataClass() {
            return Bitmap.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }

    public static class Factory implements ModelLoaderFactory<Path, Bitmap> {

        @NonNull
        @Override
        public ModelLoader<Path, Bitmap> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new DocumentThumbnailModelLoader();
        }

        @Override
        public void teardown() {}
    }
}
