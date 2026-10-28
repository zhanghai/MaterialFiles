/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.mediastore

import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import java8.nio.file.Path
import java8.nio.file.Paths
import me.zhanghai.android.files.app.application
import me.zhanghai.android.files.app.contentResolver
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object MediaStoreLister {
    private const val SCAN_TIMEOUT_SECONDS = 120L
    private const val MAX_FILES_TO_SCAN = 50_000

    fun query(category: MediaStoreCategory): List<Path> {
        scanMediaStorageSync()
        val categorySelection = category.getSelection()
        val categoryArgs = category.getSelectionArgs()
        val selection = "($categorySelection) AND ${MediaStore.MediaColumns.DATA} IS NOT NULL"
        val paths = mutableListOf<Path>()
        contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            arrayOf(MediaStore.MediaColumns.DATA),
            selection,
            categoryArgs,
            null
        )?.use { cursor ->
            val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            while (cursor.moveToNext()) {
                val data = cursor.getString(dataIndex) ?: continue
                if (data.isBlank()) continue
                if (!File(data).isFile) continue
                paths.add(Paths.get(data))
            }
        }
        return paths
    }

    private fun scanMediaStorageSync() {
        val files = collectFilesToScan()
        if (files.isEmpty()) return
        val latch = CountDownLatch(files.size)
        MediaScannerConnection.scanFile(
            application,
            files.toTypedArray(),
            null
        ) { _, _ -> latch.countDown() }
        try {
            latch.await(SCAN_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

    private fun collectFilesToScan(): List<String> {
        val rootDirs = listOfNotNull(
            getPublicDir(Environment.DIRECTORY_DCIM),
            getPublicDir(Environment.DIRECTORY_MOVIES),
            getPublicDir(Environment.DIRECTORY_PICTURES),
            getPublicDir(Environment.DIRECTORY_DOWNLOADS),
            getPublicDir(Environment.DIRECTORY_MUSIC),
            getPublicDir(Environment.DIRECTORY_PODCASTS),
            getPublicDir(Environment.DIRECTORY_ALARMS),
            getPublicDir(Environment.DIRECTORY_RINGTONES),
            getPublicDir(Environment.DIRECTORY_NOTIFICATIONS),
            getPublicDir(Environment.DIRECTORY_DOCUMENTS)
        )
        val out = mutableListOf<String>()
        for (root in rootDirs) {
            walk(root, out)
            if (out.size >= MAX_FILES_TO_SCAN) break
        }
        return out
    }

    private fun getPublicDir(name: String): File? =
        try {
            Environment.getExternalStoragePublicDirectory(name).takeIf { it.isDirectory }
        } catch (e: Exception) {
            null
        }

    private fun walk(dir: File, out: MutableList<String>) {
        if (out.size >= MAX_FILES_TO_SCAN) return
        val children = dir.listFiles() ?: return
        for (child in children) {
            if (out.size >= MAX_FILES_TO_SCAN) return
            val name = child.name
            if (name.startsWith(".")) continue
            if (child.isDirectory) {
                walk(child, out)
            } else if (child.isFile) {
                out.add(child.absolutePath)
            }
        }
    }
}
