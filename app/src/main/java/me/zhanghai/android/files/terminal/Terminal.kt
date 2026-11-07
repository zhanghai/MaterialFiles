/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.terminal

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.core.content.ContextCompat
import java8.nio.file.Path
import me.zhanghai.android.files.app.packageManager
import me.zhanghai.android.files.provider.document.documentTreeUri
import me.zhanghai.android.files.provider.document.isDocumentPath
import me.zhanghai.android.files.provider.linux.isLinuxPath
import me.zhanghai.android.files.util.getPackageInfoOrNull
import me.zhanghai.android.files.util.showToast
import me.zhanghai.android.files.util.startActivitySafe

object Terminal {
    private const val TAG = "Terminal"
    const val TERMUX_PACKAGE_NAME = "com.termux"
    const val TERMUX_PERMISSION_RUN_COMMAND = "com.termux.permission.RUN_COMMAND"
    const val TERMUX_DOCUMENTS_PROVIDER_AUTHORITY = "com.termux.documents"

    val isTermuxInstalled: Boolean
        get() = packageManager.getPackageInfoOrNull(TERMUX_PACKAGE_NAME, 0) != null

    fun resolvePath(path: Path): String? {
        if (path.isLinuxPath) {
            val linuxPath = path.toFile().path
            if (linuxPath.startsWith("/data/user/0/com.termux")) {
                return linuxPath.replaceFirst("/data/user/0/com.termux", "/data/data/com.termux")
            }
            return linuxPath
        }
        if (path.isDocumentPath) {
            val treeUri = path.documentTreeUri
            val treeDocId = try {
                DocumentsContract.getTreeDocumentId(treeUri)
            } catch (e: Exception) {
                null
            }
            if (treeUri.authority == TERMUX_DOCUMENTS_PROVIDER_AUTHORITY ||
                (treeDocId != null && (treeDocId.contains("com.termux") ||
                    treeDocId.startsWith("/data/data/com.termux") ||
                    treeDocId.startsWith("/data/user/0/com.termux")))) {
                val effectiveDocId = treeDocId ?: "/data/data/com.termux/files/home"
                val basePath = if (effectiveDocId.startsWith("/")) {
                    effectiveDocId.removeSuffix("/")
                } else {
                    "/data/data/com.termux/files/" + effectiveDocId.removeSuffix("/")
                }
                val relPath = path.toString().trimStart('/')
                val resolvedPath = if (relPath.isEmpty()) basePath else "$basePath/$relPath"
                if (resolvedPath.startsWith("/data/user/0/com.termux")) {
                    return resolvedPath.replaceFirst("/data/user/0/com.termux", "/data/data/com.termux")
                }
                return resolvedPath
            }
            if (treeUri.authority == "com.android.externalstorage.documents" &&
                treeDocId != null && treeDocId.startsWith("primary:")) {
                val relDoc = treeDocId.removePrefix("primary:").trim('/')
                val relPath = path.toString().trimStart('/')
                val sub = listOf(relDoc, relPath).filter { it.isNotEmpty() }.joinToString("/")
                return if (sub.isEmpty()) "/storage/emulated/0" else "/storage/emulated/0/$sub"
            }
        }
        return null
    }

    fun open(path: String, context: Context) {
        if (isTermuxInstalled) {
            openTermux(path, context)
        } else {
            openAndroidTerminal(path, context)
        }
    }

    fun open(path: Path, context: Context): Boolean {
        val resolved = resolvePath(path) ?: return false
        open(resolved, context)
        return true
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
