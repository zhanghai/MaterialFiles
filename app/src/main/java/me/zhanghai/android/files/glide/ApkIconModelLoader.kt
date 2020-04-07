/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide

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
import java8.nio.file.Path
import me.zhanghai.android.files.app.packageManager
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.file.guessFromPath
import me.zhanghai.android.files.file.isApk
import me.zhanghai.android.files.provider.linux.isLinuxPath

class ApkIconModelLoader : ModelLoader<Path, Drawable> {
    override fun handles(model: Path): Boolean {
        if (!model.isLinuxPath) {
            return false
        }
        return MimeType.guessFromPath(model.toFile().path).isApk
    }

    override fun buildLoadData(
        model: Path,
        width: Int,
        height: Int,
        options: Options
    ): LoadData<Drawable>? = LoadData(ObjectKey(model), Fetcher(model.toFile().path))

    private class Fetcher(private val path: String) : DataFetcher<Drawable> {
        override fun loadData(
            priority: Priority,
            callback: DataFetcher.DataCallback<in Drawable>
        ) {
            val packageInfo = packageManager.getPackageArchiveInfo(path, 0)
            if (packageInfo == null) {
                callback.onLoadFailed(
                    NullPointerException(
                        "PackageManager.getPackageArchiveInfo() returned null: $path"
                    )
                )
                return
            }
            packageInfo.applicationInfo.sourceDir = path
            packageInfo.applicationInfo.publicSourceDir = path
            val icon = packageInfo.applicationInfo.loadIcon(packageManager)
            if (icon == null) {
                callback.onLoadFailed(
                    NullPointerException(
                        "ApplicationInfo.loadIcon() returned null: $path"
                    )
                )
                return
            }
            // TODO: Add shadow for adaptive icons.
            callback.onDataReady(icon)
        }

        override fun cleanup() {}

        override fun cancel() {}

        override fun getDataClass(): Class<Drawable> = Drawable::class.java

        override fun getDataSource(): DataSource = DataSource.LOCAL
    }

    class Factory : ModelLoaderFactory<Path, Drawable> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Path, Drawable> =
            ApkIconModelLoader()

        override fun teardown() {}
    }
}
