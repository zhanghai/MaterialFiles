/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive.archiver_sevenzipjbinding;

import android.text.TextUtils;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import org.threeten.bp.Instant;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SimpleArchiveItem implements ArchiveItem {

    @NonNull
    private final String mPath;
    private final long mSize;
    private final long mPackedSize;
    private final boolean mIsDirectory;
    private final int mAttributes;
    @Nullable
    private final Instant mCreationTime;
    @Nullable
    private final Instant mLastAccessTime;
    @Nullable
    private final Instant mLastModifiedTime;
    private final boolean mEncrypted;
    private final boolean mCommented;
    @Nullable
    private final Integer mCrc;
    @Nullable
    private final String mMethod;
    @Nullable
    private final String mHostOs;
    @Nullable
    private final String mOwner;
    @Nullable
    private final String mGroup;
    @Nullable
    private final String mComment;
    private final int mIndex;
    @Nullable
    private final String mLink;

    public SimpleArchiveItem(@NonNull IInArchive archive, @NonNull ISimpleInArchiveItem item)
            throws SevenZipException {
        String path = item.getPath();
        if (TextUtils.isEmpty(path)) {
            throw new SevenZipException("ISimpleInArchiveItem.getPath() returned null or empty");
        }
        mPath = path;
        Long size = item.getSize();
        mSize = size != null && size >= 0 ? size : 0;
        Long packedSize = item.getPackedSize();
        mPackedSize = packedSize != null && packedSize >= 0 ? packedSize : 0;
        mIsDirectory = item.isFolder() || path.endsWith("/");
        Integer attributes = item.getAttributes();
        mAttributes = attributes != null ? attributes : 0;
        mCreationTime = toInstant(item.getCreationTime());
        mLastAccessTime = toInstant(item.getLastAccessTime());
        mLastModifiedTime = toInstant(item.getLastWriteTime());
        mEncrypted = item.isEncrypted();
        Boolean commented = item.isCommented();
        mCommented = commented != null ? commented : false;
        mCrc = item.getCRC();
        mMethod = item.getMethod();
        mHostOs = item.getHostOS();
        mOwner = item.getUser();
        mGroup = item.getGroup();
        mComment = item.getComment();
        mIndex = item.getItemIndex();
        mLink = archive.getStringProperty(mIndex, PropID.LINK);
    }

    @Nullable
    private static Instant toInstant(@Nullable Date date) {
        if (date != null) {
            long millis = date.getTime();
            if (millis > 0) {
                return Instant.ofEpochMilli(millis);
            }
        }
        return null;
    }

    @Override
    @NonNull
    public String getPath() {
        return mPath;
    }

    @Override
    public long getSize() {
        return mSize;
    }

    @Override
    public long getPackedSize() {
        return mPackedSize;
    }

    @Override
    public boolean isDirectory() {
        return mIsDirectory;
    }

    @Override
    public int getAttributes() {
        return mAttributes;
    }

    @Override
    @Nullable
    public Instant getCreationTime() {
        return mCreationTime;
    }

    @Override
    @Nullable
    public Instant getLastAccessTime() {
        return mLastAccessTime;
    }

    @Override
    @Nullable
    public Instant getLastModifiedTime() {
        return mLastModifiedTime;
    }

    @Override
    public boolean isEncrypted() {
        return mEncrypted;
    }

    @Override
    public boolean isCommented() {
        return mCommented;
    }

    @Override
    @Nullable
    public Integer getCrc() {
        return mCrc;
    }

    @Override
    @Nullable
    public String getMethod() {
        return mMethod;
    }

    @Override
    @Nullable
    public String getHostOs() {
        return mHostOs;
    }

    @Override
    @Nullable
    public String getOwner() {
        return mOwner;
    }

    @Override
    @Nullable
    public String getGroup() {
        return mGroup;
    }

    @Override
    @Nullable
    public String getComment() {
        return mComment;
    }

    @Override
    public int getIndex() {
        return mIndex;
    }

    @Nullable
    public String getLink() {
        return mLink;
    }
}
