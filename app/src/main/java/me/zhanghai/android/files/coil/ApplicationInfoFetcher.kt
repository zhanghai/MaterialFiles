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
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.longVersionCodeCompat
import me.zhanghai.android.files.util.getDimensionPixelSize

class ApplicationInfoFetcher(private val context: Context) : Fetcher<ApplicationInfo> {
    private val appIconLoader = AppIconLoader(
        // This is used by PrincipalListAdapter.
        context.getDimensionPixelSize(R.dimen.icon_size), false, context
    )

    override fun key(data: ApplicationInfo): String? =
        AppIconLoader.getIconKey(data, data.longVersionCodeCompat, context)

    override suspend fun fetch(
        pool: BitmapPool,
        data: ApplicationInfo,
        size: Size,
        options: Options): FetchResult {
        val icon = appIconLoader.loadIcon(data)
        // Not sampled because we only load with one fixed size.
        return DrawableResult(icon.toDrawable(context.resources), false, DataSource.DISK)
    }
}
