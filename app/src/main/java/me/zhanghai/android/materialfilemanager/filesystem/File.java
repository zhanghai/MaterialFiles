/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.zhanghai.android.materialfilemanager.file.FileTypeNames;
import me.zhanghai.android.materialfilemanager.file.MimeTypes;
import me.zhanghai.android.materialfilemanager.util.CollectionUtils;
import me.zhanghai.android.materialfilemanager.util.FileNameUtils;

public interface File extends Comparable<File>, Parcelable {

    @NonNull
    Uri getUri();

    @Nullable
    File getParent();

    @NonNull
    File getChild(@NonNull String childName);

    @NonNull
    default List<File> makeTrail() {
        List<File> trail = new ArrayList<>();
        File file = this;
        do {
            trail.add(file);
            file = file.getParent();
        } while (file != null);
        Collections.reverse(trail);
        return trail;
    }

    @NonNull
    default String getName() {
        List<String> segments = getUri().getPathSegments();
        if (segments.isEmpty()) {
            return "/";
        }
        return CollectionUtils.last(segments);
    }

    @NonNull
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

    boolean isSymbolicLink();

    boolean isSymbolicLinkBroken();

    @NonNull
    String getSymbolicLinkTarget();

    boolean isDirectory();

    default boolean isDirectoryDoNotFollowSymbolicLinks() {
        return isDirectory() && !isSymbolicLink();
    }

    @NonNull
    default String getMimeType() {
        if (isDirectory()) {
            return MimeTypes.DIRECTORY_MIME_TYPE;
        }
        return MimeTypes.getMimeType(getName());
    }

    @NonNull
    default String getTypeName(Context context) {
        String extension = FileNameUtils.getExtension(getName());
        if (isSymbolicLink() && isSymbolicLinkBroken()) {
            return FileTypeNames.getBrokenSymbolicLinkTypeName(context);
        }
        return FileTypeNames.getTypeName(getMimeType(), extension, context);
    }

    default boolean isSupportedArchive() {
        return MimeTypes.isSupportedArchive(getMimeType());
    }

    default boolean isListable() {
        return isDirectory() || isSupportedArchive();
    }

    @NonNull
    default File asListableFile() {
        if (this instanceof LocalFile && isSupportedArchive()) {
            LocalFile file = (LocalFile) this;
            return new ArchiveFile(file, Archive.pathForRoot());
        }
        return this;
    }

    long getSize();

    @NonNull
    Instant getLastModificationTime();

    @NonNull
    @WorkerThread
    List<File> getChildren() throws FileSystemException;

    @Override
    default int compareTo(@NonNull File that) {
        return getUri().compareTo(that.getUri());
    }

    default boolean equalsAsFile(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof File)) {
            return false;
        }
        File that = (File) object;
        return getUri().equals(that.getUri());
    }

    default int hashCodeAsFile() {
        return getUri().hashCode();
    }

    boolean equalsIncludingInformation(@Nullable Object object);
}
