/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.coil

import android.content.Context
import android.content.pm.ApplicationInfo
import me.zhanghai.android.files.R
import me.zhanghai.android.files.compat.PackageManagerCompat
import me.zhanghai.android.files.util.getDimensionPixelSize
import java.io.Closeable

data class AppIconPackageName(val packageName: String)

class AppIconPackageNameFetcher(private val context: Context) : AppIconFetcher<AppIconPackageName>(
    // This is used by FileListAdapter, and shrinking non-adaptive icons makes it look better as a
    // badge.
    context.getDimensionPixelSize(R.dimen.badge_size_plus_1dp), context, true
) {
    override fun key(data: AppIconPackageName): String? = data.packageName

    override fun getApplicationInfo(data: AppIconPackageName): Pair<ApplicationInfo, Closeable?> {
        // PackageManager.MATCH_UNINSTALLED_PACKAGES allows using PackageManager.MATCH_ANY_USER
        // without the INTERACT_ACROSS_USERS permission when we are in the system user and it has a
        // managed profile. It may also help corner cases like when the package is hidden.
        return context.packageManager.getApplicationInfo(
            data.packageName, PackageManagerCompat.MATCH_UNINSTALLED_PACKAGES
        ) to null
    }
}
