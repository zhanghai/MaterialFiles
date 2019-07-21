/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content;

import android.net.Uri;

import org.threeten.bp.Instant;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.attribute.FileTime;
import me.zhanghai.android.files.file.MimeTypes;

class ContentFileAttributesImpl implements BasicFileAttributes {

    @Nullable
    private final String mType;
    private final long mSize;
    @NonNull
    private final Uri mUri;

    ContentFileAttributesImpl(@Nullable String type, long size, @NonNull Uri uri) {
        mType = type;
        mSize = size;
        mUri = uri;
    }

    @NonNull
    @Override
    public FileTime lastModifiedTime() {
        return FileTime.from(Instant.ofEpochMilli(0));
    }

    @NonNull
    @Override
    public FileTime lastAccessTime() {
        return lastModifiedTime();
    }

    @NonNull
    @Override
    public FileTime creationTime() {
        return lastModifiedTime();
    }

    @Override
    public boolean isRegularFile() {
        return !isDirectory();
    }

    @Override
    public boolean isDirectory() {
        return Objects.equals(mType, MimeTypes.DIRECTORY_MIME_TYPE);
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public long size() {
        return mSize;
    }

    @NonNull
    @Override
    public Uri fileKey() {
        return mUri;
    }
}
