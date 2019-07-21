/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.system.OsConstants;

import androidx.annotation.NonNull;
import java8.nio.file.StandardOpenOption;
import me.zhanghai.android.files.provider.common.OpenOptions;
import me.zhanghai.android.files.provider.linux.syscall.Constants;

class LinuxOpenOptions {

    private LinuxOpenOptions() {}

    public static int toFlags(@NonNull OpenOptions options) {
        if (options.hasSparse()) {
            throw new UnsupportedOperationException(StandardOpenOption.SPARSE.toString());
        }
        int flags;
        if (options.hasRead() && options.hasWrite()) {
            flags = OsConstants.O_RDWR;
        } else if (options.hasWrite()) {
            flags = OsConstants.O_WRONLY;
        } else {
            flags = OsConstants.O_RDONLY;
        }
        if (options.hasAppend()) {
            flags |= OsConstants.O_APPEND;
        }
        if (options.hasTruncateExisting()) {
            flags |= OsConstants.O_TRUNC;
        }
        if (options.hasCreateNew()) {
            flags |= OsConstants.O_CREAT | OsConstants.O_EXCL;
        } else if (options.hasCreate()) {
            flags |= OsConstants.O_CREAT;
        }
        if (options.hasSync()) {
            flags |= OsConstants.O_SYNC;
        }
        if (options.hasDsync()) {
            flags |= Constants.O_DSYNC;
        }
        if (options.hasNoFollowLinks() || (!options.hasCreateNew() && options.hasDeleteOnClose())) {
            flags |= OsConstants.O_NOFOLLOW;
        }
        return flags;
    }
}
