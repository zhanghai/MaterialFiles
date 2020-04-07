/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide

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
import me.zhanghai.android.files.provider.common.newInputStream
import me.zhanghai.android.files.util.closeSafe
import java.io.InputStream

class PathInputStreamModelLoader : ModelLoader<Path, InputStream> {
    override fun handles(model: Path): Boolean = true

    override fun buildLoadData(
        model: Path,
        width: Int,
        height: Int,
        options: Options
    ): LoadData<InputStream>? {
        if (MediaStoreUtil.isThumbnailSize(width, height) && !model.shouldLoadThumbnail) {
            return null
        }
        return LoadData(ObjectKey(model), Fetcher(model))
    }

    private class Fetcher(private val path: Path) : DataFetcher<InputStream> {
        private var inputStream: InputStream? = null

        override fun loadData(
            priority: Priority,
            callback: DataFetcher.DataCallback<in InputStream>
        ) {
            inputStream = try {
                path.newInputStream()
            } catch (e: Exception) {
                callback.onLoadFailed(e)
                return
            }
            callback.onDataReady(inputStream)
        }

        override fun cleanup() {
            inputStream?.closeSafe()
        }

        override fun cancel() {}

        override fun getDataClass(): Class<InputStream> = InputStream::class.java

        override fun getDataSource(): DataSource = DataSource.LOCAL
    }

    class Factory : ModelLoaderFactory<Path, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Path, InputStream> =
            PathInputStreamModelLoader()

        override fun teardown() {}
    }
}
