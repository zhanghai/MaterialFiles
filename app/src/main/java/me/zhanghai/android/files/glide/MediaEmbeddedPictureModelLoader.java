/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide;

import android.media.MediaMetadataRetriever;
import android.os.ParcelFileDescriptor;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.data.mediastore.MediaStoreUtil;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.provider.document.DocumentFileSystemProvider;
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;

public class MediaEmbeddedPictureModelLoader implements ModelLoader<Path, ByteBuffer> {

    @Override
    public boolean handles(@NonNull Path model) {
        if (!(LinuxFileSystemProvider.isLinuxPath(model)
                || DocumentFileSystemProvider.isDocumentPath(model))) {
            return false;
        }
        Path fileName = model.getFileName();
        if (fileName == null) {
            return false;
        }
        String mimeType = MimeTypes.getMimeType(fileName.toString());
        return MimeTypes.isMedia(mimeType);
    }

    @Nullable
    @Override
    public LoadData<ByteBuffer> buildLoadData(@NonNull Path model, int width, int height,
                                              @NonNull Options options) {
        if (MediaStoreUtil.isThumbnailSize(width, height) && !GlidePathUtils.shouldLoadThumbnail(
                model)) {
            return null;
        }
        return new LoadData<>(new ObjectKey(model), new Fetcher(model));
    }

    private static class Fetcher implements DataFetcher<ByteBuffer> {

        @NonNull
        private final Path path;

        public Fetcher(@NonNull Path path) {
            this.path = path;
        }

        @Override
        public void loadData(@NonNull Priority priority,
                             @NonNull DataCallback<? super ByteBuffer> callback) {
            ByteBuffer picture;
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                try {
                    setDataSource(retriever, path);
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

        private static void setDataSource(@NonNull MediaMetadataRetriever retriever,
                                          @NonNull Path path) throws Exception {
            if (LinuxFileSystemProvider.isLinuxPath(path)) {
                retriever.setDataSource(path.toFile().toString());
            } else if (DocumentFileSystemProvider.isDocumentPath(path)) {
                try (ParcelFileDescriptor pfd = DocumentResolver.openParcelFileDescriptor(
                        (DocumentResolver.Path) path, "r")) {
                    retriever.setDataSource(pfd.getFileDescriptor());
                }
            } else {
                throw new AssertionError(path);
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
