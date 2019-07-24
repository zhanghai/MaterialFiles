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

    @Nullable
    private final String mMimeType;
    private final long mSize;
    private final long mLastModifiedTimeMillis;
    @NonNull
    private final Uri mUri;

    DocumentFileAttributesImpl(@Nullable String mimeType, long size, long lastModifiedTimeMillis,
                               @NonNull Uri uri) {
        mMimeType = mimeType;
        mSize = size;
        mLastModifiedTimeMillis = lastModifiedTimeMillis;
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

    @NonNull
    @Override
    public Uri fileKey() {
        return mUri;
    }
}
