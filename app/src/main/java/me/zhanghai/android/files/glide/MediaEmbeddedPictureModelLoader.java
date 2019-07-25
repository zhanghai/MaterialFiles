/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide;

import android.media.MediaMetadataRetriever;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;

public class MediaEmbeddedPictureModelLoader implements ModelLoader<Path, ByteBuffer> {

    @Override
    public boolean handles(@NonNull Path model) {
        if (!LinuxFileSystemProvider.isLinuxPath(model)) {
            return false;
        }
        String mimeType = MimeTypes.getMimeType(model.toFile().toString());
        return MimeTypes.isMedia(mimeType);
    }

    @Nullable
    @Override
    public LoadData<ByteBuffer> buildLoadData(@NonNull Path model, int width, int height,
                                              @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model), new Fetcher(model.toFile().toString()));
    }

    private static class Fetcher implements DataFetcher<ByteBuffer> {

        @NonNull
        private final String path;

        public Fetcher(@NonNull String path) {
            this.path = path;
        }

        @Override
        public void loadData(@NonNull Priority priority,
                             @NonNull DataCallback<? super ByteBuffer> callback) {
            ByteBuffer picture;
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                try {
                    retriever.setDataSource(path);
                    picture = ByteBuffer.wrap(retriever.getEmbeddedPicture());
                } finally {
                    retriever.release();
                }
            } catch (Exception e) {
                callback.onLoadFailed(e);
                return;
            }
            callback.onDataReady(picture);
        }

        @Override
        public void cleanup() {}

        @Override
        public void cancel() {}

        @NonNull
        @Override
        public Class<ByteBuffer> getDataClass() {
            return ByteBuffer.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }

    public static class Factory implements ModelLoaderFactory<Path, ByteBuffer> {

        @NonNull
        @Override
        public ModelLoader<Path, ByteBuffer> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new MediaEmbeddedPictureModelLoader();
        }

        @Override
        public void teardown() {}
    }
}
