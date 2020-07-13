/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive

import java8.nio.file.StandardOpenOption
import me.zhanghai.android.files.provider.common.OpenOptions

internal fun OpenOptions.checkForArchive() {
    if (write) {
        throw UnsupportedOperationException(StandardOpenOption.WRITE.toString())
    }
    if (append) {
        throw UnsupportedOperationException(StandardOpenOption.APPEND.toString())
    }
    if (truncateExisting) {
        throw UnsupportedOperationException(StandardOpenOption.TRUNCATE_EXISTING.toString())
    }
    if (create) {
        throw UnsupportedOperationException(StandardOpenOption.CREATE.toString())
    }
    if (createNew) {
        throw UnsupportedOperationException(StandardOpenOption.CREATE_NEW.toString())
    }
    if (deleteOnClose) {
        throw UnsupportedOperationException(StandardOpenOption.DELETE_ON_CLOSE.toString())
    }
    if (sync) {
        throw UnsupportedOperationException(StandardOpenOption.SYNC.toString())
    }
    if (dsync) {
        throw UnsupportedOperationException(StandardOpenOption.DSYNC.toString())
    }
}
