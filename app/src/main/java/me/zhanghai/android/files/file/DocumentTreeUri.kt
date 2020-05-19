/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.file

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.provider.DocumentsContract
import kotlinx.android.parcel.Parcelize
import me.zhanghai.android.files.app.contentResolver
import me.zhanghai.android.files.compat.DocumentsContractCompat
import me.zhanghai.android.files.navigation.DocumentTreesLiveData
import me.zhanghai.android.files.util.releasePersistablePermission
import me.zhanghai.android.files.util.takePersistablePermission

// TODO: https://youtrack.jetbrains.com/issue/KT-37384
//inline class DocumentTreeUri(val value: Uri) {
@Parcelize
data class DocumentTreeUri(val value: Uri): Parcelable {
    val documentId: String
        get() = DocumentsContract.getTreeDocumentId(value)

    companion object {
        val persistedUris: List<DocumentTreeUri>
            get() =
                contentResolver.persistedUriPermissions
                    .filter { it.uri.isDocumentTreeUri }
                    .sortedBy { it.persistedTime }
                    .map { it.uri.asDocumentTreeUri() }

        // TODO: Consider StorageVolume.createAccessIntent().
        fun createOpenIntent(): Intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    }
}

fun Uri.asDocumentTreeUriOrNull(): DocumentTreeUri? =
    if (isDocumentTreeUri) DocumentTreeUri(
        this
    ) else null

fun Uri.asDocumentTreeUri(): DocumentTreeUri {
    check(isDocumentTreeUri)
    return DocumentTreeUri(this)
}

private val Uri.isDocumentTreeUri: Boolean
    get() = DocumentsContractCompat.isTreeUri(this)

fun DocumentTreeUri.buildDocumentUri(documentId: String): DocumentUri =
    DocumentsContract.buildDocumentUriUsingTree(value, documentId).asDocumentUri()

val DocumentTreeUri.displayName: String?
    get() = buildDocumentUri(documentId).displayName

val DocumentTreeUri.displayNameOrUri: String
    get() = displayName ?: value.toString()

fun DocumentTreeUri.takePersistablePermission(): Boolean =
    if (value.takePersistablePermission(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        ) || value.takePersistablePermission(Intent.FLAG_GRANT_READ_URI_PERMISSION)) {
        DocumentTreesLiveData.loadValue()
        true
    } else {
        false
    }

fun DocumentTreeUri.releasePersistablePermission(): Boolean =
    if (value.releasePersistablePermission(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )) {
        DocumentTreesLiveData.loadValue()
        true
    } else {
        false
    }
