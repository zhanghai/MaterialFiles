/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide

import android.graphics.Bitmap
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
import me.zhanghai.android.files.provider.content.resolver.ResolverException
import me.zhanghai.android.files.provider.document.isDocumentPath
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver

class DocumentThumbnailModelLoader : ModelLoader<Path, Bitmap> {
    override fun handles(model: Path): Boolean = model.isDocumentPath

    override fun buildLoadData(
        model: Path,
        width: Int,
        height: Int,
        options: Options
    ): LoadData<Bitmap>? {
        // @see MediaStoreImageThumbLoader#buildLoadData(Uri, int, int, Options)
        if (!MediaStoreUtil.isThumbnailSize(width, height)) {
            return null
        }
        return LoadData(ObjectKey(model), Fetcher(model as DocumentResolver.Path, width, height))
    }

    private class Fetcher(
        private val path: DocumentResolver.Path,
        private val width: Int,
        private val height: Int
    ) : DataFetcher<Bitmap> {
        override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
            val thumbnail = try {
                DocumentResolver.getThumbnail(path, width, height)
            } catch (e: ResolverException) {
                callback.onLoadFailed(e)
                return
            }
            callback.onDataReady(thumbnail)
        }

        override fun cleanup() {}

        override fun cancel() {}

        override fun getDataClass(): Class<Bitmap> = Bitmap::class.java

        override fun getDataSource(): DataSource = DataSource.LOCAL
    }

    class Factory : ModelLoaderFactory<Path, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Path, Bitmap> =
            DocumentThumbnailModelLoader()

        override fun teardown() {}
    }
}
