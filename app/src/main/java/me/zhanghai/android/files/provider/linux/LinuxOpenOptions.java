/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.system.OsConstants;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.provider.common.OpenOptions;
import me.zhanghai.android.files.provider.linux.syscall.Constants;

class LinuxOpenOptions {

    private LinuxOpenOptions() {}

    public static int toFlags(@NonNull OpenOptions options) {
        int flags;
        if (options.hasRead() && options.hasWrite()) {
            flags = OsConstants.O_RDWR;
        } else if (options.hasWrite()) {
            flags = OsConstants.O_WRONLY;
        } else {
            flags = OsConstants.O_RDONLY;
        }
        if (options.hasTruncateExisting()) {
            flags |= OsConstants.O_TRUNC;
        }
        if (options.hasAppend()) {
            flags |= OsConstants.O_APPEND;
        }
        if (options.hasCreateNew()) {
            flags |= OsConstants.O_CREAT | OsConstants.O_EXCL;
        } else if (options.hasCreate()) {
            flags |= OsConstants.O_CREAT;
        }
        if (options.hasNoFollowLinks() || (!options.hasCreateNew() && options.hasDeleteOnClose())) {
            flags |= OsConstants.O_NOFOLLOW;
        }
        if (options.hasDsync()) {
            flags |= Constants.O_DSYNC;
        }
        if (options.hasSync()) {
            flags |= OsConstants.O_SYNC;
        }
        return flags;
    }
}
