/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.glide;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.TextUtils;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.File;
import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import me.zhanghai.android.materialfilemanager.file.MimeTypes;

public class MediaEmbeddedPictureModelLoader<Model> implements ModelLoader<Model, ByteBuffer> {

    @Override
    public boolean handles(@NonNull Model model) {
        String path = getPath(model);
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        String mimeType = MimeTypes.getMimeType(path);
        return MimeTypes.isMedia(mimeType);
    }

    @Nullable
    @Override
    public LoadData<ByteBuffer> buildLoadData(@NonNull Model model, int width, int height,
                                              @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model), new DataFetcher(getPath(model)));
    }

    @NonNull
    private String getPath(@NonNull Model model) {
        if (model instanceof String) {
            return (String) model;
        } else if (model instanceof File) {
            File file = (File) model;
            return file.getPath();
        } else if (model instanceof Uri) {
            Uri uri = (Uri) model;
            if (TextUtils.equals(uri.getScheme(), "file")) {
                return uri.getPath();
            }
        }
        throw new IllegalArgumentException("Unable to get path from model: " + model);
    }

    private static class DataFetcher
            implements com.bumptech.glide.load.data.DataFetcher<ByteBuffer> {

        @NonNull
        private final String path;

        public DataFetcher(@NonNull String path) {
            this.path = path;
        }

        @Override
        public void loadData(@NonNull Priority priority,
                             @NonNull DataCallback<? super ByteBuffer> callback) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(path);
                ByteBuffer picture = ByteBuffer.wrap(retriever.getEmbeddedPicture());
                callback.onDataReady(picture);
            } catch (Exception e) {
                callback.onLoadFailed(e);
            } finally {
                retriever.release();
            }
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

    public static class Factory<Model> implements ModelLoaderFactory<Model, ByteBuffer> {

        @NonNull
        @Override
        public ModelLoader<Model, ByteBuffer> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new MediaEmbeddedPictureModelLoader<>();
        }

        @Override
        public void teardown() {}
    }
}
