/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content;

import android.net.Uri;

import org.threeten.bp.Instant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.attribute.FileTime;
import me.zhanghai.android.files.provider.common.ContentProviderFileAttributes;

class ContentFileAttributesImpl implements ContentProviderFileAttributes {

    @Nullable
    private final String mMimeType;
    private final long mSize;
    @NonNull
    private final Uri mUri;

    ContentFileAttributesImpl(@Nullable String mimeType, long size, @NonNull Uri uri) {
        mMimeType = mimeType;
        mSize = size;
        mUri = uri;
    }

    @NonNull
    @Override
    public FileTime lastModifiedTime() {
        return FileTime.from(Instant.EPOCH);
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
