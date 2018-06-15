/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;

import java.util.List;

import me.zhanghai.android.materialfilemanager.functional.Functional;

public class Files {

    private Files() {}

    public static File create(Uri path) {
        switch (path.getScheme()) {
            case LocalFile.SCHEME:
                return new JavaLocalFile(path);
            case ArchiveFile.SCHEME:
                return new ArchiveFile(path);
            default:
                throw new UnsupportedOperationException("Unknown path: " + path);
        }
    }

    public static void onTrailChanged(List<File> path) {
        List<java.io.File> archiveJavaFiles = Functional.map(
                Functional.filter(path, file -> file instanceof ArchiveFile),
                file -> ((ArchiveFile) file).getArchiveFile().makeJavaFile());
        Archive.retainCache(archiveJavaFiles);
    }

    public static void invalidateCache(Uri path) {
        switch (path.getScheme()) {
            case ArchiveFile.SCHEME:
                Archive.invalidateCache(new ArchiveFile(path).getArchiveFile().makeJavaFile());
                break;
        }
    }
}
