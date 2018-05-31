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

    private List<File> mFileList;

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
        if (!isListable()) {
            throw new IllegalStateException("File is not listable");
        }
        return mFileList;
    }

    @Override
    public final void loadFileList() {
        if (!isListable()) {
            throw new IllegalStateException("File is not listable");
        }
        onLoadFileList();
    }

    protected abstract void onLoadFileList();

    protected void setFileList(List<File> fileList) {
        mFileList = fileList;
    }
}
