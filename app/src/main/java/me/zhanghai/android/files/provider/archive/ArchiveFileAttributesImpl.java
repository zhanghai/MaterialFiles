/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import org.threeten.bp.Instant;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import java8.nio.file.attribute.FileTime;
import me.zhanghai.android.files.provider.archive.archiver_sevenzipjbinding.ArchiveItem;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.PosixFileAttributes;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.provider.common.PosixFileType;
import me.zhanghai.android.files.provider.common.PosixFileTypes;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixUser;

class ArchiveFileAttributesImpl implements PosixFileAttributes {

    @NonNull
    private final Path mArchiveFile;
    @NonNull
    private final ArchiveItem mItem;

    ArchiveFileAttributesImpl(@NonNull Path archiveFile, @NonNull ArchiveItem item) {
        mArchiveFile = archiveFile;
        mItem = item;
    }

    @NonNull
    public String getItemPath() {
        return mItem.getPath();
    }

    @NonNull
    @Override
    public FileTime lastModifiedTime() {
        Instant lastModifiedTime = mItem.getLastModifiedTime();
        if (lastModifiedTime == null) {
            lastModifiedTime = Instant.EPOCH;
        }
        return FileTime.from(lastModifiedTime);
    }

    @NonNull
    @Override
    public FileTime lastAccessTime() {
        Instant lastAccessTime = mItem.getLastAccessTime();
        if (lastAccessTime != null) {
            return FileTime.from(lastAccessTime);
        }
        return lastModifiedTime();
    }

    @NonNull
    @Override
    public FileTime creationTime() {
        Instant creationTime = mItem.getCreationTime();
        if (creationTime != null) {
            return FileTime.from(creationTime);
        }
        return lastModifiedTime();
    }

    @NonNull
    public PosixFileType type() {
        return PosixFileTypes.fromArchiveItem(mItem);
    }

    @Override
    public long size() {
        return mItem.getSize();
    }

    @NonNull
    @Override
    public ArchiveFileKey fileKey() {
        return new ArchiveFileKey(mArchiveFile, mItem.getPath());
    }

    @Nullable
    @Override
    public PosixUser owner() {
        String owner = mItem.getOwner();
        // TODO: Where is ID?
        return null;
    }

    @Nullable
    @Override
    public PosixGroup group() {
        String group = mItem.getGroup();
        // TODO: Where is ID?
        return null;
    }

    @Nullable
    public Set<PosixFileModeBit> mode() {
        // TODO
        return null;
    }

    @Nullable
    @Override
    public ByteString seLinuxContext() {
        return null;
    }
}
