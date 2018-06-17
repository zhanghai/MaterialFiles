/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.content.Context;

public class FileSystemException extends Exception {

    private int mMessageRes;

    public FileSystemException(int messageRes) {
        super();

        mMessageRes = messageRes;
    }

    public FileSystemException(int messageRes, Throwable cause) {
        super(cause);

        mMessageRes = messageRes;
    }

    public String getMessage(Context context) {
        return context.getString(mMessageRes);
    }
}
