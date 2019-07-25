/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.attribute.FileTime;

public class ParcelableContentProviderFileAttributes implements ContentProviderFileAttributes,
        Parcelable {

    @NonNull
    private final FileTime mLastModifiedTime;
    @NonNull
    private final FileTime mLastAccessTime;
    @NonNull
    private final FileTime mCreationTime;
    @Nullable
    private final String mMimeType;
    private final long mSize;
    @NonNull
    private final Parcelable mFileKey;

    public ParcelableContentProviderFileAttributes(
            @NonNull ContentProviderFileAttributes attributes) {
        mLastModifiedTime = attributes.lastModifiedTime();
        mLastAccessTime = attributes.lastAccessTime();
        mCreationTime = attributes.creationTime();
        mMimeType = attributes.mimeType();
        mSize = attributes.size();
        mFileKey = (Parcelable) attributes.fileKey();
    }

    @Override
    @NonNull
    public FileTime lastModifiedTime() {
        return mLastModifiedTime;
    }

    @Override
    @NonNull
    public FileTime lastAccessTime() {
        return mLastAccessTime;
    }

    @Override
    @NonNull
    public FileTime creationTime() {
        return mCreationTime;
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

    @Override
    @NonNull
    public Parcelable fileKey() {
        return mFileKey;
    }


    public static final Creator<ParcelableContentProviderFileAttributes> CREATOR =
            new Creator<ParcelableContentProviderFileAttributes>() {
                @Override
                public ParcelableContentProviderFileAttributes createFromParcel(Parcel source) {
                    return new ParcelableContentProviderFileAttributes(source);
                }
                @Override
                public ParcelableContentProviderFileAttributes[] newArray(int size) {
                    return new ParcelableContentProviderFileAttributes[size];
                }
            };

    protected ParcelableContentProviderFileAttributes(Parcel in) {
        mLastModifiedTime = ((ParcelableFileTime) in.readParcelable(
                ParcelableFileTime.class.getClassLoader())).get();
        mLastAccessTime = ((ParcelableFileTime) in.readParcelable(
                ParcelableFileTime.class.getClassLoader())).get();
        mCreationTime = ((ParcelableFileTime) in.readParcelable(
                ParcelableFileTime.class.getClassLoader())).get();
        mMimeType = in.readString();
        mSize = in.readLong();
        mFileKey = in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(new ParcelableFileTime(mLastModifiedTime), flags);
        dest.writeParcelable(new ParcelableFileTime(mLastAccessTime), flags);
        dest.writeParcelable(new ParcelableFileTime(mCreationTime), flags);
        dest.writeString(mMimeType);
        dest.writeLong(mSize);
        dest.writeParcelable(mFileKey, flags);
    }
}
