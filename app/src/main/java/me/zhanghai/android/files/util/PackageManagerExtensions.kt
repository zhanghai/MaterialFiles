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

fun PackageManager.getPackageInfoOrNull(packageName: String, flags: Int): PackageInfo? =
    getPackageManagerInfoOrNull { getPackageInfo(packageName, flags) }

fun PackageManager.getPermissionInfoOrNull(permissionName: String, flags: Int): PermissionInfo? =
    getPackageManagerInfoOrNull { getPermissionInfo(permissionName, flags) }

@OptIn(ExperimentalContracts::class)
private inline fun <T> getPackageManagerInfoOrNull(block: () -> T): T? {
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
