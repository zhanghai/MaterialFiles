/*
 * Copyright (c) 2026 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.documentsprovider

import android.net.Uri
import android.provider.DocumentsContract
import java8.nio.file.Path
import java8.nio.file.Paths
import me.zhanghai.android.files.BuildConfig
import java.io.FileNotFoundException
import java.net.URI

/**
 * Document IDs are the [java.net.URI] string of the path, e.g. `file:///storage/emulated/0/a.txt`
 * or `smb://user@host/share/a.txt`, so any path we can browse can be served.
 */
val Path.documentId: String
    get() = toUri().toString()

fun String.toDocumentPathOrNull(): Path? =
    try {
        Paths.get(URI.create(this))
    } catch (e: Exception) {
        null
    }

@Throws(FileNotFoundException::class)
fun String.toDocumentPath(): Path =
    toDocumentPathOrNull() ?: throw FileNotFoundException("Invalid document ID: $this")

val Path.documentsProviderUri: Uri
    get() = DocumentsContract.buildDocumentUri(BuildConfig.DOCUMENTS_PROVIDER_AUTHORITY, documentId)

val Path.documentsProviderTreeUri: Uri
    get() =
        DocumentsContract.buildTreeDocumentUri(
            BuildConfig.DOCUMENTS_PROVIDER_AUTHORITY, documentId
        )
