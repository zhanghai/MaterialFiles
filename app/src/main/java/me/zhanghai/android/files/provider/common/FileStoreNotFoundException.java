/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java8.nio.file.FileSystemException;

public class FileStoreNotFoundException extends FileSystemException {

    public FileStoreNotFoundException(String file) {
        super(file);
    }
}
