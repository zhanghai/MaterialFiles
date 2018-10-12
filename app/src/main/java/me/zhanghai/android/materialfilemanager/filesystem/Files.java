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

    public static File ofUri(Uri uri) {
        switch (uri.getScheme()) {
            case LocalFile.SCHEME:
                return new LocalFile(uri);
            case ArchiveFile.SCHEME:
                return new ArchiveFile(uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    public static File ofLocalPath(String path) {
        return new LocalFile(LocalFile.uriFromPath(path));
    }

    public static File childOf(File file, String name) {
        Uri uri = file.getUri().buildUpon().appendPath(name).build();
        return ofUri(uri);
    }

    public static void onTrailChanged(List<File> path) {
        Set<java.io.File> archiveJavaFiles = Functional.map(
                Functional.filter(path, file -> file instanceof ArchiveFile),
                file -> ((ArchiveFile) file).getArchiveFile().makeJavaFile(), new HashSet<>());
        Archive.retainCache(archiveJavaFiles);
    }

    public static void invalidateCache(File file) {
        if (file instanceof ArchiveFile) {
            ArchiveFile archiveFile = (ArchiveFile) file;
            java.io.File archiveJavaFile = archiveFile.getArchiveFile().makeJavaFile();
            Archive.invalidateCache(archiveJavaFile);
        }
    }
}
