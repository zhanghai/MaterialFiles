/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filesystem;

import android.net.Uri;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.functional.Functional;

public class Files {

    private Files() {}

    @NonNull
    public static File ofUri(@NonNull Uri uri) {
        switch (uri.getScheme()) {
            case LocalFile.SCHEME:
                return new LocalFile(uri);
            case ArchiveFile.SCHEME:
                return new ArchiveFile(uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @NonNull
    public static File ofLocalPath(@NonNull String path) {
        return new LocalFile(LocalFile.uriFromPath(path));
    }

    @NonNull
    public static File childOf(@NonNull File file, @NonNull String name) {
        Uri uri = file.getUri().buildUpon().appendPath(name).build();
        return ofUri(uri);
    }

    public static void onTrailChanged(@NonNull List<File> path) {
        Set<String> archiveJavaFiles = Functional.map(
                Functional.filter(path, file -> file instanceof ArchiveFile),
                file -> ((ArchiveFile) file).getArchiveFile().getPath(), new HashSet<>());
        Archive.retainCache(archiveJavaFiles);
    }

    public static void invalidateCache(@NonNull File file) {
        if (file instanceof ArchiveFile) {
            ArchiveFile archiveFile = (ArchiveFile) file;
            Archive.invalidateCache(archiveFile.getArchiveFile().getPath());
        }
    }
}
