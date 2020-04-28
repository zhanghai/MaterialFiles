/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide

import android.media.MediaMetadataRetriever
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
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.file.guessFromPath
import me.zhanghai.android.files.file.isMedia
import me.zhanghai.android.files.provider.document.isDocumentPath
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver
import me.zhanghai.android.files.provider.linux.isLinuxPath
import java.nio.ByteBuffer

class MediaEmbeddedPictureModelLoader : ModelLoader<Path, ByteBuffer> {
    override fun handles(model: Path): Boolean {
        if (!(model.isLinuxPath || model.isDocumentPath)) {
            return false
        }
        val fileName = model.fileName ?: return false
        return MimeType.guessFromPath(fileName.toString()).isMedia
    }

    override fun buildLoadData(
        model: Path, width: Int, height: Int,
        options: Options
    ): LoadData<ByteBuffer>? {
        if (MediaStoreUtil.isThumbnailSize(width, height) && !model.shouldLoadThumbnail) {
            return null
        }
        return LoadData(ObjectKey(model), Fetcher(model))
    }

    private class Fetcher(private val path: Path) : DataFetcher<ByteBuffer> {
        override fun loadData(
            priority: Priority,
            callback: DataFetcher.DataCallback<in ByteBuffer>
        ) {
            val picture = try {
                val retriever = MediaMetadataRetriever()
                try {
                    setDataSource(retriever, path)
                    ByteBuffer.wrap(retriever.embeddedPicture)
                } finally {
                    retriever.release()
                }
            } catch (e: Exception) {
                callback.onLoadFailed(e)
                return
            }
            callback.onDataReady(picture)
        }

        override fun cleanup() {}

        override fun cancel() {}

        override fun getDataClass(): Class<ByteBuffer> = ByteBuffer::class.java

        override fun getDataSource(): DataSource = DataSource.LOCAL

        companion object {
            @Throws(Exception::class)
            private fun setDataSource(retriever: MediaMetadataRetriever, path: Path) {
                when {
                    path.isLinuxPath -> retriever.setDataSource(path.toFile().toString())
                    path.isDocumentPath ->
                        DocumentResolver.openParcelFileDescriptor(
                            (path as DocumentResolver.Path), "r"
                        ).use { pfd -> retriever.setDataSource(pfd.fileDescriptor) }
                    else -> throw AssertionError(path)
                }
            }
        }
    }

    class Factory : ModelLoaderFactory<Path, ByteBuffer> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Path, ByteBuffer> =
            MediaEmbeddedPictureModelLoader()

        override fun teardown() {}
    }
}
