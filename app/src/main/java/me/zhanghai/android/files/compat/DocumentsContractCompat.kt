/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.compat

import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract

object DocumentsContractCompat {
    const val EXTRA_INITIAL_URI = "android.provider.extra.INITIAL_URI"
    const val EXTRA_SHOW_ADVANCED = "android.provider.extra.SHOW_ADVANCED"
    const val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"
    const val EXTERNAL_STORAGE_PRIMARY_EMULATED_ROOT_ID = "primary"

    fun isTreeUri(uri: Uri): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            DocumentsContract.isTreeUri(uri)
        } else {
            uri.pathSegments.let { it.size >= 2 && it.first() == "tree" }
        }
}
