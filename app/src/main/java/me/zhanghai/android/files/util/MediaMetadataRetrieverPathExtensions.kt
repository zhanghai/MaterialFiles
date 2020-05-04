/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.util

import android.media.MediaMetadataRetriever
import java8.nio.file.Path
import me.zhanghai.android.files.provider.document.isDocumentPath
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver
import me.zhanghai.android.files.provider.linux.isLinuxPath

val Path.isMediaMetadataRetrieverCompatible: Boolean
    get() = isLinuxPath || isDocumentPath

fun MediaMetadataRetriever.setDataSource(path: Path) {
    when {
        path.isLinuxPath -> setDataSource(path.toFile().path)
        path.isDocumentPath ->
            DocumentResolver.openParcelFileDescriptor(path as DocumentResolver.Path, "r")
                .use { pfd -> setDataSource(pfd.fileDescriptor) }
        else -> throw IllegalArgumentException(path.toString())
    }
}
