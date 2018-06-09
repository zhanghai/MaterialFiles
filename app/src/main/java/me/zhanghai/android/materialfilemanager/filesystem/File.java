/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.threeten.bp.Instant;

import java.util.List;

import me.zhanghai.android.materialfilemanager.R;
import me.zhanghai.android.materialfilemanager.file.MimeTypes;
import me.zhanghai.android.materialfilemanager.util.CollectionUtils;

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

    @WorkerThread
    void loadInformation();

    long getSize();

    Instant getModified();

    @NonNull
    String getDescription(Context context);

    boolean isDirectory();

    @NonNull
    default String getMimeType() {
        if (isDirectory()) {
            return MimeTypes.MIME_TYPE_DIRECTORY;
        }
        return MimeTypes.getMimeType(getPath());
    }

    default boolean isSupportedArchive() {
        return MimeTypes.isSupportedArchive(getMimeType());
    }

    default boolean isListable() {
        return isDirectory() || isSupportedArchive();
    }

    @NonNull
    java.io.File makeJavaFile();

    @WorkerThread
    void loadFileList();

    @NonNull
    List<File> getFileList();

    boolean equals(Object object);

    int hashCode();
}
