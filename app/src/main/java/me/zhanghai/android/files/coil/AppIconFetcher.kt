/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.coil

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import me.zhanghai.android.appiconloader.AppIconLoader
import java.io.Closeable

class AppIconFetcher(
    private val options: Options,
    private val appIconLoader: AppIconLoader,
    private val getApplicationInfo: () -> Pair<ApplicationInfo, Closeable?>
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        val (applicationInfo, closeable) = getApplicationInfo()
        val icon = closeable.use { appIconLoader.loadIcon(applicationInfo) }
        // Not sampled because we only load with one fixed size.
        return DrawableResult(icon.toDrawable(options.context.resources), false, DataSource.DISK)
    }

    abstract class Factory<T : Any>(
        iconSize: Int,
        context: Context,
        shrinkNonAdaptiveIcons: Boolean = false
    ) : Fetcher.Factory<T> {
        private val appIconLoader =
            AppIconLoader(iconSize, shrinkNonAdaptiveIcons, context)

        override fun create(data: T, options: Options, imageLoader: ImageLoader): Fetcher =
            AppIconFetcher(options, appIconLoader) { getApplicationInfo(data) }

        abstract fun getApplicationInfo(data: T): Pair<ApplicationInfo, Closeable?>
    }
}
