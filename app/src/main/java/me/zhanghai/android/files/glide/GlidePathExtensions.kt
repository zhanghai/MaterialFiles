/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.glide

import java8.nio.file.Path
import me.zhanghai.android.files.provider.document.isDocumentPath
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.valueCompat

val Path.shouldLoadThumbnail: Boolean
    get() {
        if (isDocumentPath) {
            if (!(DocumentResolver.isLocal((this as DocumentResolver.Path))
                    || Settings.READ_REMOTE_FILES_FOR_THUMBNAIL.valueCompat)) {
                return false
            }
        }
        return true
    }
