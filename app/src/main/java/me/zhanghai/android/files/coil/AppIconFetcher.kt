/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.coil

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.core.graphics.drawable.toDrawable
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.size.Size
import me.zhanghai.android.appiconloader.AppIconLoader
import java.io.Closeable

abstract class AppIconFetcher<T : Any>(
    iconSize: Int,
    private val context: Context,
    shrinkNonAdaptiveIcons: Boolean = false
) : Fetcher<T> {
    private val appIconLoader = AppIconLoader(iconSize, shrinkNonAdaptiveIcons, context)

    abstract fun getApplicationInfo(data: T): Pair<ApplicationInfo, Closeable?>

    override suspend fun fetch(
        pool: BitmapPool,
        data: T,
        size: Size,
        options: Options
    ): FetchResult {
        val (applicationInfo, closeable) = getApplicationInfo(data)
        val icon = closeable.use { appIconLoader.loadIcon(applicationInfo) }
        // Not sampled because we only load with one fixed size.
        return DrawableResult(icon.toDrawable(context.resources), false, DataSource.DISK)
    }
}
