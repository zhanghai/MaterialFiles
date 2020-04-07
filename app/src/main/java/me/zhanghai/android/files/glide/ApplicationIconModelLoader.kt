/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey
import me.zhanghai.android.files.app.packageManager
import me.zhanghai.android.files.compat.longVersionCodeCompat

class ApplicationIconModelLoader : ModelLoader<ApplicationInfo, Drawable> {
    override fun handles(model: ApplicationInfo): Boolean = true

    override fun buildLoadData(
        model: ApplicationInfo,
        width: Int,
        height: Int,
        options: Options
    ): LoadData<Drawable>? {
        val key = "${model.packageName}:${model.longVersionCodeCompat}"
        return LoadData(ObjectKey(key), Fetcher(model))
    }

    private class Fetcher(private val applicationInfo: ApplicationInfo) : DataFetcher<Drawable> {
        override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Drawable>) {
            val icon = packageManager.getApplicationIcon(applicationInfo)
            // TODO: Add shadow for adaptive icons.
            callback.onDataReady(icon)
        }

        override fun cleanup() {}

        override fun cancel() {}

        override fun getDataClass(): Class<Drawable> = Drawable::class.java

        override fun getDataSource(): DataSource = DataSource.LOCAL
    }

    class Factory : ModelLoaderFactory<ApplicationInfo, Drawable> {
        override fun build(
            multiFactory: MultiModelLoaderFactory
        ): ModelLoader<ApplicationInfo, Drawable> = ApplicationIconModelLoader()

        override fun teardown() {}
    }
}
