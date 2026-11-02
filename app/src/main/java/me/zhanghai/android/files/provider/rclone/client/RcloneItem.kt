/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.rclone.client

import org.json.JSONObject

data class RcloneItem(
    val path: String,
    val name: String,
    val size: Long,
    val mimeType: String?,
    val modTime: String?,
    val isDir: Boolean
) {
    companion object {
        fun fromJson(json: JSONObject): RcloneItem {
            val path = json.optString("Path", "")
            val name = json.optString("Name", "")
            val size = json.optLong("Size", -1L)
            val mimeType = json.optString("MimeType").takeIf { it.isNotEmpty() }
            val modTime = json.optString("ModTime").takeIf { it.isNotEmpty() }
            val isDir = json.optBoolean("IsDir", false)
            return RcloneItem(path, name, size, mimeType, modTime, isDir)
        }
    }
}
