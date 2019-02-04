/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import androidx.annotation.NonNull;
import java8.nio.file.StandardOpenOption;
import me.zhanghai.android.files.provider.common.OpenOptions;

class ArchiveOpenOptions {

    private ArchiveOpenOptions() {}

    static void check(@NonNull OpenOptions options) {
        if (options.hasWrite()) {
            throw new UnsupportedOperationException(StandardOpenOption.WRITE.toString());
        } else if (options.hasAppend()) {
            throw new UnsupportedOperationException(StandardOpenOption.APPEND.toString());
        } else if (options.hasTruncateExisting()) {
            throw new UnsupportedOperationException(
                    StandardOpenOption.TRUNCATE_EXISTING.toString());
        } else if (options.hasCreate()) {
            throw new UnsupportedOperationException(StandardOpenOption.CREATE.toString());
        } else if (options.hasCreateNew()) {
            throw new UnsupportedOperationException(StandardOpenOption.CREATE_NEW.toString());
        } else if (options.hasDeleteOnClose()) {
            throw new UnsupportedOperationException(StandardOpenOption.DELETE_ON_CLOSE.toString());
        } else if (options.hasSparse()) {
            throw new UnsupportedOperationException(StandardOpenOption.SPARSE.toString());
        } else if (options.hasSync()) {
            throw new UnsupportedOperationException(StandardOpenOption.SYNC.toString());
        } else if (options.hasDsync()) {
            throw new UnsupportedOperationException(StandardOpenOption.DSYNC.toString());
        }
    }
}
