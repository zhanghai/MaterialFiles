/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.terminal

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import me.zhanghai.android.files.app.packageManager
import me.zhanghai.android.files.util.getPackageInfoOrNull
import me.zhanghai.android.files.util.showToast
import me.zhanghai.android.files.util.startActivitySafe

object Terminal {
    private const val TAG = "Terminal"
    const val TERMUX_PACKAGE_NAME = "com.termux"
    const val TERMUX_PERMISSION_RUN_COMMAND = "com.termux.permission.RUN_COMMAND"

    val isTermuxInstalled: Boolean
        get() = packageManager.getPackageInfoOrNull(TERMUX_PACKAGE_NAME, 0) != null

    fun open(path: String, context: Context) {
        if (isTermuxInstalled) {
            openTermux(path, context)
        } else {
            openAndroidTerminal(path, context)
        }
    }

    fun openTermux(path: String, context: Context) {
        val intent = Intent().apply {
            setClassName(TERMUX_PACKAGE_NAME, "com.termux.app.RunCommandService")
            action = "com.termux.RUN_COMMAND"
            putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/login")
            putExtra("com.termux.RUN_COMMAND_WORKDIR", path)
            putExtra("com.termux.RUN_COMMAND_BACKGROUND", false)
            putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", "0")
        }
        try {
            ContextCompat.startForegroundService(context, intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Termux service", e)
            context.showToast(e.localizedMessage ?: "Failed to start Termux")
        }
    }

    fun openAndroidTerminal(path: String, context: Context) {
        val componentName =
            packageManager.queryIntentActivities(Intent(Intent.ACTION_SEND).setType("*/*"), 0)
                .firstOrNull { it.activityInfo.name.endsWith(".TermHere") }?.activityInfo
                ?.let { ComponentName(it.packageName, it.name) }
                ?: ComponentName("jackpal.androidterm", "jackpal.androidterm.TermHere")
        val intent = Intent()
            .setComponent(componentName)
            .setAction(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
        context.startActivitySafe(intent)
    }
}
