/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.terminal

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import me.zhanghai.android.files.app.packageManager
import me.zhanghai.android.files.util.startActivitySafe
import java.io.File

object Terminal {
    fun open(path: String, context: Context) {
        val uri = Uri.fromFile(File(path))
        val componentName =
            packageManager.queryIntentActivities(Intent(Intent.ACTION_SEND).setType("text/plain"), 0)
                .firstOrNull { it.activityInfo.name.endsWith(".TermHere") }?.activityInfo
                ?.let { ComponentName(it.packageName, it.name) }
                ?: ComponentName("jackpal.androidterm", "jackpal.androidterm.TermHere")
        val intent = Intent()
            .setComponent(componentName)
            .setAction(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivitySafe(intent)
    }
}
