/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.mediastore

import android.provider.MediaStore
import java8.nio.file.Path
import java8.nio.file.Paths
import me.zhanghai.android.files.app.contentResolver

object MediaStoreLister {
    fun query(category: MediaStoreCategory): List<Path> {
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
                if (!java.io.File(data).isFile) continue
                paths.add(Paths.get(data))
            }
        }
        return paths
    }
}
