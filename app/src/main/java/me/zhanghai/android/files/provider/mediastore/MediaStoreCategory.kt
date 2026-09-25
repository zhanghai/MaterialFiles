/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.mediastore

import android.os.Build
import android.provider.MediaStore
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import me.zhanghai.android.files.R

enum class MediaStoreCategory(
    val segment: String,
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int
) {
    IMAGES(
        "images",
        R.drawable.image_icon_white_24dp,
        R.string.navigation_media_category_images
    ),
    VIDEOS(
        "videos",
        R.drawable.video_icon_white_24dp,
        R.string.navigation_media_category_videos
    ),
    AUDIO(
        "audio",
        R.drawable.audio_icon_white_24dp,
        R.string.navigation_media_category_audio
    ),
    DOCUMENTS(
        "documents",
        R.drawable.document_icon_white_24dp,
        R.string.navigation_media_category_documents
    ),
    APKS(
        "apks",
        R.drawable.apk_icon_white_24dp,
        R.string.navigation_media_category_apks
    );

    fun getSelection(): String = when (this) {
        IMAGES -> "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
        VIDEOS -> "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.MediaColumns.MIME_TYPE} LIKE ?)"
        AUDIO -> "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
        DOCUMENTS -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
        } else {
            val placeholders = DOCUMENT_MIME_TYPES.joinToString(",") { "?" }
            "${MediaStore.MediaColumns.MIME_TYPE} IN ($placeholders)"
        }
        APKS -> "${MediaStore.MediaColumns.MIME_TYPE} = ?"
    }

    fun getSelectionArgs(): Array<String> = when (this) {
        IMAGES -> arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())
        VIDEOS -> arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(), "video%")
        AUDIO -> arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString())
        DOCUMENTS -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(MEDIA_TYPE_DOCUMENT.toString())
        } else {
            DOCUMENT_MIME_TYPES.toTypedArray()
        }
        APKS -> arrayOf("application/vnd.android.package-archive")
    }

    companion object {
        // MediaStore.Files.FileColumns.MEDIA_TYPE_DOCUMENT, added in API 30
        private const val MEDIA_TYPE_DOCUMENT = 6

        private val DOCUMENT_MIME_TYPES = listOf(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.oasis.opendocument.text",
            "application/vnd.oasis.opendocument.spreadsheet",
            "application/vnd.oasis.opendocument.presentation",
            "application/rtf",
            "text/plain",
            "text/csv",
            "application/epub+zip",
            "application/json",
            "application/xml"
        )

        fun fromSegment(segment: String): MediaStoreCategory? =
            values().find { it.segment == segment }
    }
}
