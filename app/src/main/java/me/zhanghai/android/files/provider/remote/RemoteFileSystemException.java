/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import java.io.IOException;

import androidx.annotation.Nullable;

public class RemoteFileSystemException extends IOException {

    public RemoteFileSystemException() {}

    public RemoteFileSystemException(@Nullable String message) {
        super(message);
    }

    public RemoteFileSystemException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public RemoteFileSystemException(@Nullable Throwable cause) {
        super(cause);
    }
}
