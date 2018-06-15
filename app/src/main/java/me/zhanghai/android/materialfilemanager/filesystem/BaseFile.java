/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.support.annotation.NonNull;

public abstract class BaseFile implements File {

    protected Uri mPath;

    public BaseFile(Uri path) {
        mPath = path;
    }

    @NonNull
    public Uri getPath() {
        return mPath;
    }
}
