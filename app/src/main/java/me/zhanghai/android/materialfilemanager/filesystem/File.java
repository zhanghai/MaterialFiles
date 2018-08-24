/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.threeten.bp.Instant;

import java.util.List;

import me.zhanghai.android.materialfilemanager.file.MimeTypes;
import me.zhanghai.android.materialfilemanager.util.CollectionUtils;
import me.zhanghai.android.materialfilemanager.util.FileNameUtils;

public interface File extends Parcelable {

    @NonNull
    Uri getUri();

    @NonNull
    List<File> makeBreadcrumbPath();

    @NonNull
    default String getName() {
        List<String> segments = getUri().getPathSegments();
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

    boolean hasInformation();

    default void loadInformation() throws FileSystemException {
        if (!hasInformation()) {
            reloadInformation();
        }
    }

    @WorkerThread
    void reloadInformation() throws FileSystemException;

    long getSize();

    Instant getLastModificationTime();

    boolean isDirectory();

    boolean isSymbolicLink();

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
            // FIXME
            return new ArchiveFile((LocalFile) this, Archive.pathForRoot());
        }
        return this;
    }

    @WorkerThread
    List<File> getChildren() throws FileSystemException;

    default void startObserving(Runnable observer) {}

    default boolean isObserving() {
        return false;
    }

    default void stopObserving() {}

    boolean equals(Object object);

    int hashCode();
}
