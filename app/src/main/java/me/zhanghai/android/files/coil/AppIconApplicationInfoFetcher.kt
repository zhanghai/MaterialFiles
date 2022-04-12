/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.coil

import android.content.Context
import android.content.pm.ApplicationInfo
import me.zhanghai.android.appiconloader.AppIconLoader
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.longVersionCodeCompat
import me.zhanghai.android.files.util.getDimensionPixelSize
import java.io.Closeable

class AppIconApplicationInfoFetcher(private val context: Context) : AppIconFetcher<ApplicationInfo>(
    // This is used by PrincipalListAdapter.
    context.getDimensionPixelSize(R.dimen.icon_size), context
) {
    override fun key(data: ApplicationInfo): String? =
        AppIconLoader.getIconKey(data, data.longVersionCodeCompat, context)

    override fun getApplicationInfo(data: ApplicationInfo): Pair<ApplicationInfo, Closeable?> =
        data to null
}
