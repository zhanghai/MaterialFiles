/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.content.Context;

import androidx.annotation.NonNull;

public class FileSystemException extends Exception {

    private int mMessageRes;

    public FileSystemException(int messageRes) {
        super();

        mMessageRes = messageRes;
    }

    public FileSystemException(@NonNull Throwable cause) {
        super(cause);
    }

    /**
     * @deprecated This is ugly.
     */
    public FileSystemException(int messageRes, @NonNull Throwable cause) {
        super(cause);

        mMessageRes = messageRes;
    }

    @NonNull
    public String getMessage(@NonNull Context context) {
        return mMessageRes != 0 ? context.getString(mMessageRes) : getCause().getMessage();
    }
}
