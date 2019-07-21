/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content;

import androidx.annotation.NonNull;
import java8.nio.file.StandardOpenOption;
import me.zhanghai.android.files.provider.common.OpenOptions;

class ContentOpenOptions {

    private ContentOpenOptions() {}

    public static String toMode(@NonNull OpenOptions options) {
        StringBuilder mode = new StringBuilder();
        if (options.hasRead() && options.hasWrite()) {
            mode.append("rw");
        } else if (options.hasWrite()) {
            mode.append('w');
        } else {
            mode.append('r');
        }
        if (options.hasAppend()) {
            mode.append('a');
        }
        if (options.hasTruncateExisting()) {
            mode.append('t');
        }
        if (options.hasCreateNew()) {
            throw new UnsupportedOperationException(StandardOpenOption.CREATE_NEW.toString());
        } else if (options.hasCreate()) {
            // Ignored.
        }
        if (options.hasDeleteOnClose()) {
            throw new UnsupportedOperationException(StandardOpenOption.DELETE_ON_CLOSE.toString());
        }
        if (options.hasSparse()) {
            throw new UnsupportedOperationException(StandardOpenOption.SPARSE.toString());
        }
        if (options.hasSync()) {
            throw new UnsupportedOperationException(StandardOpenOption.SYNC.toString());
        }
        if (options.hasDsync()) {
            throw new UnsupportedOperationException(StandardOpenOption.DSYNC.toString());
        }
        return mode.toString();
    }
}
