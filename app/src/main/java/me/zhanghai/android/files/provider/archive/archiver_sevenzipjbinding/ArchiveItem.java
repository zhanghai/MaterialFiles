/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver_sevenzipjbinding;

import org.threeten.bp.Instant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface ArchiveItem {

    @NonNull
    String getPath();

    long getSize();

    long getPackedSize();

    boolean isDirectory();

    int getAttributes();

    @Nullable
    Instant getCreationTime();

    @Nullable
    Instant getLastAccessTime();

    @Nullable
    Instant getLastModifiedTime();

    boolean isEncrypted();

    boolean isCommented();

    @Nullable
    Integer getCrc();

    @Nullable
    String getMethod();

    @Nullable
    String getHostOs();

    @Nullable
    String getOwner();

    @Nullable
    String getGroup();

    @Nullable
    String getComment();

    int getIndex();

    @Nullable
    String getLink();
}
