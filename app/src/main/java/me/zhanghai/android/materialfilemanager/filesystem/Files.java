/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.zhanghai.android.materialfilemanager.functional.Functional;

public class Files {

    private Files() {}

    public static File ofPath(Uri path) {
        switch (path.getScheme()) {
            case LocalFile.SCHEME:
                return new JavaLocalFile(path);
            case ArchiveFile.SCHEME:
                return new ArchiveFile(path);
            default:
                throw new UnsupportedOperationException("Unknown path: " + path);
        }
    }

    public static File childOf(File file, String name) {
        Uri path = file.getPath().buildUpon().appendPath(name).build();
        return ofPath(path);
    }

    public static void onTrailChanged(List<File> path) {
        Set<java.io.File> archiveJavaFiles = Functional.map(
                Functional.filter(path, file -> file instanceof ArchiveFile),
                file -> ((ArchiveFile) file).getArchiveFile().makeJavaFile(), new HashSet<>());
        Archive.retainCache(archiveJavaFiles);
    }

    public static void invalidateCache(Uri path) {
        switch (path.getScheme()) {
            case ArchiveFile.SCHEME: {
                java.io.File archiveJavaFile = new ArchiveFile(path).getArchiveFile()
                        .makeJavaFile();
                Archive.invalidateCache(archiveJavaFile);
                break;
            }
        }
    }
}
