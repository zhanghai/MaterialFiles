/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class LocalFile extends BaseFile {

    public LocalFile(Uri path) {
        super(path);
    }

    @NonNull
    @Override
    public List<File> makeFilePath() {
        List<File> path = new ArrayList<>();
        java.io.File file = makeJavaFile();
        while (file != null) {
            // TODO
            path.add(new JavaLocalFile(Uri.fromFile(file)));
            file = file.getParentFile();
        }
        Collections.reverse(path);
        return path;
    }

    @NonNull
    @Override
    public java.io.File makeJavaFile() {
        return new java.io.File(mPath.getPath());
    }
}
