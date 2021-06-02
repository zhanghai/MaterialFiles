/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.SigningInfo
import android.os.Build
import me.zhanghai.android.files.util.andInv
import me.zhanghai.android.files.util.hasBits

fun PackageManager.getPackageArchiveInfoCompat(archiveFilePath: String, flags: Int): PackageInfo? {
    var packageInfo = getPackageArchiveInfo(archiveFilePath, flags)
    // getPackageArchiveInfo() returns null for unsigned APKs if signing info is requested.
    if (packageInfo == null) {
        val flagsWithoutGetSigningInfo = flags.andInv(
            @Suppress("DEPRECATION")
            PackageManager.GET_SIGNATURES or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                0
            }
        )
        if (flags != flagsWithoutGetSigningInfo) {
            packageInfo = getPackageArchiveInfo(archiveFilePath, flagsWithoutGetSigningInfo)
                ?.apply {
                    @Suppress("DEPRECATION")
                    if (flags.hasBits(PackageManager.GET_SIGNATURES)) {
                        signatures = emptyArray()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                        && flags.hasBits(PackageManager.GET_SIGNING_CERTIFICATES)) {
                        signingInfo = SigningInfo()
                    }
                }
        }
    }
    return packageInfo
}
