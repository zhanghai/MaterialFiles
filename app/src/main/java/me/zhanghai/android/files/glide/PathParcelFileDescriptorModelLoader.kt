/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide

import android.os.ParcelFileDescriptor
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.data.mediastore.MediaStoreUtil
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import java8.nio.file.Path
import me.zhanghai.android.files.provider.document.isDocumentPath
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver.openParcelFileDescriptor
import me.zhanghai.android.files.provider.linux.isLinuxPath
import me.zhanghai.android.files.util.closeSafe

class PathParcelFileDescriptorModelLoader : ModelLoader<Path, ParcelFileDescriptor> {
    override fun handles(model: Path): Boolean = model.isLinuxPath || model.isDocumentPath

    override fun buildLoadData(
        model: Path,
        width: Int,
        height: Int,
        options: Options
    ): LoadData<ParcelFileDescriptor>? {
        if (MediaStoreUtil.isThumbnailSize(width, height) && !model.shouldLoadThumbnail) {
            return null
        }
        return LoadData(ObjectKey(model), Fetcher(model))
    }

    private class Fetcher(private val path: Path) : DataFetcher<ParcelFileDescriptor> {
        private var parcelFileDescriptor: ParcelFileDescriptor? = null

        override fun loadData(
            priority: Priority,
            callback: DataFetcher.DataCallback<in ParcelFileDescriptor>
        ) {
            parcelFileDescriptor = try {
                when {
                    path.isLinuxPath ->
                        ParcelFileDescriptor.open(
                            path.toFile(), ParcelFileDescriptor.MODE_READ_ONLY
                        )
                    path.isDocumentPath ->
                        openParcelFileDescriptor(path as DocumentResolver.Path, "r")
                    else -> throw AssertionError(path)
                }
            } catch (e: Exception) {
                callback.onLoadFailed(e)
                return
            }
            callback.onDataReady(parcelFileDescriptor)
        }

        override fun cleanup() {
            parcelFileDescriptor?.closeSafe()
        }

        override fun cancel() {}

        override fun getDataClass(): Class<ParcelFileDescriptor> = ParcelFileDescriptor::class.java

        override fun getDataSource(): DataSource = DataSource.LOCAL
    }

    class Factory : ModelLoaderFactory<Path, ParcelFileDescriptor> {
        override fun build(
            multiFactory: MultiModelLoaderFactory
        ): ModelLoader<Path, ParcelFileDescriptor> = PathParcelFileDescriptorModelLoader()

        override fun teardown() {}
    }
}
