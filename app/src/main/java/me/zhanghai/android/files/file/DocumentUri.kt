/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import me.zhanghai.android.files.app.contentResolver

inline class DocumentUri(val value: Uri) {
    val documentId: String
        get() = DocumentsContract.getDocumentId(value)
}

fun Uri.asDocumentUriOrNull(): DocumentUri? =
    if (isDocumentUri) DocumentUri(this) else null

fun Uri.asDocumentUri(): DocumentUri {
    require(isDocumentUri)
    return DocumentUri(this)
}

private val Uri.isDocumentUri: Boolean
    /** @see DocumentsContract.isDocumentUri */
    get() {
        if (scheme != ContentResolver.SCHEME_CONTENT) {
            return false
        }
        val paths = pathSegments
        return when (paths.size) {
            2 -> paths[0] == "document"
            4 -> paths[0] == "tree" && paths[2] == "document"
            else -> false
        }
    }

val DocumentUri.displayName: String?
    get() {
        try {
            contentResolver.query(
                value, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null
            ).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME
                    )
                    if (displayNameIndex != -1) {
                        val displayName = cursor.getString(displayNameIndex)
                        if (!displayName.isNullOrEmpty()) {
                            return displayName
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

val DocumentUri.displayNameOrUri: String
    get() = displayName ?: value.toString()
