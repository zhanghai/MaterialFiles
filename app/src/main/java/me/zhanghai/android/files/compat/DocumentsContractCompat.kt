/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract

object DocumentsContractCompat {
    const val EXTRA_INITIAL_URI = "android.provider.extra.INITIAL_URI"
    const val EXTRA_SHOW_ADVANCED = "android.provider.extra.SHOW_ADVANCED"

    const val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"
    const val EXTERNAL_STORAGE_PRIMARY_EMULATED_ROOT_ID = "primary"

    private const val PATH_DOCUMENT = "document"
    private const val PATH_CHILDREN = "children"
    private const val PATH_TREE = "tree"

    /** @see DocumentsContract.isDocumentUri */
    fun isDocumentUri(uri: Uri): Boolean {
        if (uri.scheme != ContentResolver.SCHEME_CONTENT) {
            return false
        }
        val pathSegments = uri.pathSegments
        return when (pathSegments.size) {
            2 -> pathSegments[0] == PATH_DOCUMENT
            4 -> pathSegments[0] == PATH_TREE && pathSegments[2] == PATH_DOCUMENT
            else -> false
        }
    }

    fun isTreeUri(uri: Uri): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DocumentsContract.isTreeUri(uri)
        } else {
            uri.pathSegments.let { it.size >= 2 && it[0] == PATH_TREE }
        }

    fun isChildDocumentsUri(uri: Uri): Boolean {
        val pathSegments = uri.pathSegments
        return when (pathSegments.size) {
            3 -> pathSegments[0] == PATH_DOCUMENT && pathSegments[2] == PATH_CHILDREN
            5 ->
                pathSegments[0] == PATH_TREE && pathSegments[2] == PATH_DOCUMENT
                    && pathSegments[4] == PATH_CHILDREN
            else -> false
        }
    }
}
