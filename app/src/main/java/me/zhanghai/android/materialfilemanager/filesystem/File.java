/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.threeten.bp.Instant;

import java.util.List;

import me.zhanghai.android.materialfilemanager.file.MimeTypes;
import me.zhanghai.android.materialfilemanager.util.CollectionUtils;
import me.zhanghai.android.materialfilemanager.util.FileNameUtils;

public interface File {

    @NonNull
    Uri getPath();

    @NonNull
    List<File> makeFilePath();

    @NonNull
    default String getName() {
        List<String> segments = getPath().getPathSegments();
        if (segments.isEmpty()) {
            return "/";
        }
        return CollectionUtils.last(segments);
    }

    default String getExtension() {
        if (isDirectory()) {
            return "";
        } else {
            return FileNameUtils.getExtension(getName());
        }
    }

    @WorkerThread
    void loadInformation();

    long getSize();

    Instant getLastModified();

    boolean isDirectory();

    @NonNull
    default String getMimeType() {
        if (isDirectory()) {
            return MimeTypes.MIME_TYPE_DIRECTORY;
        }
        return MimeTypes.getMimeType(getName());
    }

    default boolean isSupportedArchive() {
        return MimeTypes.isSupportedArchive(getMimeType());
    }

    default boolean isListable() {
        return isDirectory() || isSupportedArchive();
    }

    default File asListableFile() {
        if (!(this instanceof ArchiveFile) && isSupportedArchive()) {
            return new ArchiveFile(this, Uri.parse("/"));
        }
        return this;
    }

    @NonNull
    java.io.File makeJavaFile();

    @WorkerThread
    List<File> loadFileList();

    boolean equals(Object object);

    int hashCode();
}
