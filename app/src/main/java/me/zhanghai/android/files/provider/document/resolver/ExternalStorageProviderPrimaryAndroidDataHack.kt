/*
 * Copyright (c) 2021 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.document.resolver

import android.database.Cursor
import android.database.MergeCursor
import android.net.Uri
import android.provider.DocumentsContract
import me.zhanghai.android.files.compat.DocumentsContractCompat
import me.zhanghai.android.files.provider.content.resolver.requireString

// On Android 11, ExternalStorageProvider no longer returns Android/data and Android/obb as children
// of the Android directory on primary storage. However, the two child directories are actually
// still accessible.
object ExternalStorageProviderHack {
    private const val EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_DOCUMENT_ID = "primary:Android"
    private const val EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_DATA_DOCUMENT_ID =
        "primary:Android/data"
    private const val EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_OBB_DOCUMENT_ID =
        "primary:Android/obb"

    fun transformQueryResult(uri: Uri, cursor: Cursor): Cursor {
        if (uri.authority == DocumentsContractCompat.EXTERNAL_STORAGE_PROVIDER_AUTHORITY
            && DocumentsContractCompat.isChildDocumentsUri(uri)
            && DocumentsContract.getDocumentId(uri)
                == EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_DOCUMENT_ID) {
            var hasDataRow = false
            var hasObbRow = false
            try {
                while (cursor.moveToNext()) {
                    when (cursor.requireString(DocumentsContract.Document.COLUMN_DOCUMENT_ID)) {
                        EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_DATA_DOCUMENT_ID ->
                            hasDataRow = true
                        EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_OBB_DOCUMENT_ID ->
                            hasObbRow = true
                    }
                    if (hasDataRow && hasObbRow) {
                        break
                    }
                }
            } finally {
                cursor.moveToPosition(-1)
            }
            if (hasDataRow && hasObbRow) {
                return cursor
            }
            val cursors = mutableListOf(cursor)
            if (!hasDataRow) {
                val androidDataUri = DocumentsContract.buildDocumentUriUsingTree(
                    uri, EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_DATA_DOCUMENT_ID
                )
                cursors += DocumentResolver.query(androidDataUri, null, null)
            }
            if (!hasObbRow) {
                val androidObbUri = DocumentsContract.buildDocumentUriUsingTree(
                    uri, EXTERNAL_STORAGE_PROVIDER_PRIMARY_ANDROID_OBB_DOCUMENT_ID
                )
                cursors += DocumentResolver.query(androidObbUri, null, null)
            }
            return MergeCursor(cursors.toTypedArray())
        } else {
            return cursor
        }
    }
}
