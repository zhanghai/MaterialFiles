/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun PackageManager.getPackageInfoSafe(packageName: String, flags: Int): PackageInfo? =
    getPackageManagerInfoSafe { getPackageInfo(packageName, flags) }

fun PackageManager.getPermissionInfoSafe(permissionName: String, flags: Int): PermissionInfo? =
    getPackageManagerInfoSafe { getPermissionInfo(permissionName, flags) }

@OptIn(ExperimentalContracts::class)
private inline fun <T> getPackageManagerInfoSafe(block: () -> T): T? {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return try {
        block()
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }
}
