/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.document;

import android.net.Uri;

import org.threeten.bp.Instant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.attribute.FileTime;
import me.zhanghai.android.files.provider.common.ContentProviderFileAttributes;

class DocumentFileAttributesImpl implements ContentProviderFileAttributes {

    private final long mLastModifiedTimeMillis;
    @Nullable
    private final String mMimeType;
    private final long mSize;
    private final int mFlags;
    @NonNull
    private final Uri mUri;

    DocumentFileAttributesImpl(long lastModifiedTimeMillis, @Nullable String mimeType, long size,
                               int flags, @NonNull Uri uri) {
        mLastModifiedTimeMillis = lastModifiedTimeMillis;
        mMimeType = mimeType;
        mSize = size;
        mFlags = flags;
        mUri = uri;
    }

    @NonNull
    @Override
    public FileTime lastModifiedTime() {
        return FileTime.from(Instant.ofEpochMilli(mLastModifiedTimeMillis));
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

    @Nullable
    @Override
    public String mimeType() {
        return mMimeType;
    }

    @Override
    public long size() {
        return mSize;
    }

    public int getFlags() {
        return mFlags;
    }

    @NonNull
    @Override
    public Uri fileKey() {
        return mUri;
    }
}
