/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.app.AppOpsManager
import android.os.Build

object AppOpsManagerCompat {
    val OPSTR_REQUEST_INSTALL_PACKAGES =
        AppOpsManager.permissionToOp(android.Manifest.permission.REQUEST_INSTALL_PACKAGES)!!
}

fun AppOpsManager.checkOpRawNoThrowCompat(
    op: String,
    uid: Int,
    packageName: String,
    attributionTag: String?,
): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        checkOpRawNoThrow(op, uid, packageName, attributionTag)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // checkOp*() never accepted attributionTag until Baklava so it's safe to ignore.
        @Suppress("DEPRECATION") unsafeCheckOpRawNoThrow(op, uid, packageName)
    } else {
        // checkOp() was always raw before Q, despite that checkOpNoThrow() was always converting
        // MODE_FOREGROUND to MODE_ALLOWED.
        try {
            checkOp(op, uid, packageName)
        } catch (_: SecurityException) {
            AppOpsManager.MODE_ERRORED
        }
    }
