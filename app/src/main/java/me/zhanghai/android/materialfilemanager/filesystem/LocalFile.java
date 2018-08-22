/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class LocalFile extends BaseFile {

    public static final String SCHEME = "file";

    protected static Uri uriFromPath(String path) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .path(path)
                .build();
    }

    protected static String uriToPath(Uri uri) {
        return uri.getPath();
    }

    protected static String joinPaths(String parent, String child) {
        return new java.io.File(parent, child).getPath();
    }

    public LocalFile(Uri uri) {
        super(uri);
    }

    @NonNull
    @Override
    public List<File> makeBreadcrumbPath() {
        List<File> breadcrumbPath = new ArrayList<>();
        java.io.File javaFile = makeJavaFile();
        while (javaFile != null) {
            File file = Files.ofUri(uriFromPath(javaFile.getPath()));
            breadcrumbPath.add(file);
            javaFile = javaFile.getParentFile();
        }
        Collections.reverse(breadcrumbPath);
        return breadcrumbPath;
    }

    @NonNull
    public String getPath() {
        return uriToPath(mUri);
    }

    @NonNull
    public java.io.File makeJavaFile() {
        return new java.io.File(getPath());
    }


    protected LocalFile(Parcel in) {
        super(in);
    }
}
