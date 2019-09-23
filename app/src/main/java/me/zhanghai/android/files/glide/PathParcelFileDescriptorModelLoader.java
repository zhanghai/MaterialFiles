/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide;

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

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.document.DocumentFileSystemProvider;
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;

public class PathParcelFileDescriptorModelLoader
        implements ModelLoader<Path, ParcelFileDescriptor> {

    @Override
    public boolean handles(@NonNull Path model) {
        return LinuxFileSystemProvider.isLinuxPath(model)
                || DocumentFileSystemProvider.isDocumentPath(model);
    }

    @Nullable
    @Override
    public LoadData<ParcelFileDescriptor> buildLoadData(@NonNull Path model, int width, int height,
                                                        @NonNull Options options) {
        if (MediaStoreUtil.isThumbnailSize(width, height) && !GlidePathUtils.shouldLoadThumbnail(
                model)) {
            return null;
        }
        return new LoadData<>(new ObjectKey(model), new Fetcher(model));
    }

    private static class Fetcher implements DataFetcher<ParcelFileDescriptor> {

        @NonNull
        private final Path path;

        private ParcelFileDescriptor parcelFileDescriptor;

        public Fetcher(@NonNull Path path) {
            this.path = path;
        }

        @Override
        public void loadData(@NonNull Priority priority,
                             @NonNull DataCallback<? super ParcelFileDescriptor> callback) {
            try {
                if (LinuxFileSystemProvider.isLinuxPath(path)) {
                    parcelFileDescriptor = ParcelFileDescriptor.open(path.toFile(),
                            ParcelFileDescriptor.MODE_READ_ONLY);
                } else if (DocumentFileSystemProvider.isDocumentPath(path)) {
                    parcelFileDescriptor = DocumentResolver.openParcelFileDescriptor(
                            (DocumentResolver.Path) path, "r");
                } else {
                    throw new AssertionError(path);
                }
            } catch (Exception e) {
                callback.onLoadFailed(e);
                return;
            }
            callback.onDataReady(parcelFileDescriptor);
        }

        @Override
        public void cleanup() {
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void cancel() {}

        @NonNull
        @Override
        public Class<ParcelFileDescriptor> getDataClass() {
            return ParcelFileDescriptor.class;
        }

        @NonNull
        @Override
        public DataSource getDataSource() {
            return DataSource.LOCAL;
        }
    }

    public static class Factory implements ModelLoaderFactory<Path, ParcelFileDescriptor> {

        @NonNull
        @Override
        public ModelLoader<Path, ParcelFileDescriptor> build(
                @NonNull MultiModelLoaderFactory multiFactory) {
            return new PathParcelFileDescriptorModelLoader();
        }

        @Override
        public void teardown() {}
    }
}
