/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import java.io.IOException;

import androidx.annotation.NonNull;

public class RemoteFileSystemException extends IOException {

    public RemoteFileSystemException(@NonNull Exception cause) {
        super(cause);
    }
}
