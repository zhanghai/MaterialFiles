/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filejob;

import java.io.IOException;

public class FileJobUiException extends IOException {

    public FileJobUiException() {}

    public FileJobUiException(String message) {
        super(message);
    }

    public FileJobUiException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileJobUiException(Throwable cause) {
        super(cause);
    }
}
