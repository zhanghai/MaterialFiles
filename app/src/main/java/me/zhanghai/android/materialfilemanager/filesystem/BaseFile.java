/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Objects;

public abstract class BaseFile<I> implements File {

    protected Uri mPath;
    protected I mInformation;
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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        LocalFile that = (LocalFile) object;
        return Objects.equals(mPath, that.mPath) && Objects.equals(mInformation, that.mInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPath, mInformation);
    }
}
