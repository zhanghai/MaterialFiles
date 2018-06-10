/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.List;

public abstract class BaseFile implements File {

    protected Uri mPath;
    protected List<File> mFileList;

    public BaseFile(Uri path) {
        mPath = path;
    }

    @NonNull
    public Uri getPath() {
        return mPath;
    }

    @NonNull
    @Override
    public List<File> getFileList() {
        return mFileList;
    }
}
