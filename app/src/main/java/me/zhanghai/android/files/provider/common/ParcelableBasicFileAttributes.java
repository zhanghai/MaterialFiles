/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.attribute.FileTime;

public class ParcelableBasicFileAttributes implements BasicFileAttributes, Parcelable {

    @NonNull
    private final FileTime mLastModifiedTime;
    @NonNull
    private final FileTime mLastAccessTime;
    @NonNull
    private final FileTime mCreationTime;
    private final boolean mIsRegularFile;
    private final boolean mIsDirectory;
    private final boolean mIsSymbolicLink;
    private final boolean mIsOther;
    private final long mSize;
    @NonNull
    private final Parcelable mFileKey;

    public ParcelableBasicFileAttributes(@NonNull BasicFileAttributes attributes) {
        mLastModifiedTime = attributes.lastModifiedTime();
        mLastAccessTime = attributes.lastAccessTime();
        mCreationTime = attributes.creationTime();
        mIsRegularFile = attributes.isRegularFile();
        mIsDirectory = attributes.isDirectory();
        mIsSymbolicLink = attributes.isSymbolicLink();
        mIsOther = attributes.isOther();
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

    @Override
    public boolean isRegularFile() {
        return mIsRegularFile;
    }

    @Override
    public boolean isDirectory() {
        return mIsDirectory;
    }

    @Override
    public boolean isSymbolicLink() {
        return mIsSymbolicLink;
    }

    @Override
    public boolean isOther() {
        return mIsOther;
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


    public static final Creator<ParcelableBasicFileAttributes> CREATOR =
            new Creator<ParcelableBasicFileAttributes>() {
                @Override
                public ParcelableBasicFileAttributes createFromParcel(Parcel source) {
                    return new ParcelableBasicFileAttributes(source);
                }
                @Override
                public ParcelableBasicFileAttributes[] newArray(int size) {
                    return new ParcelableBasicFileAttributes[size];
                }
            };

    protected ParcelableBasicFileAttributes(Parcel in) {
        mLastModifiedTime = ((ParcelableFileTime) in.readParcelable(
                ParcelableFileTime.class.getClassLoader())).get();
        mLastAccessTime = ((ParcelableFileTime) in.readParcelable(
                ParcelableFileTime.class.getClassLoader())).get();
        mCreationTime = ((ParcelableFileTime) in.readParcelable(
                ParcelableFileTime.class.getClassLoader())).get();
        mIsRegularFile = in.readByte() != 0;
        mIsDirectory = in.readByte() != 0;
        mIsSymbolicLink = in.readByte() != 0;
        mIsOther = in.readByte() != 0;
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
        dest.writeByte(mIsRegularFile ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsDirectory ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsSymbolicLink ? (byte) 1 : (byte) 0);
        dest.writeByte(mIsOther ? (byte) 1 : (byte) 0);
        dest.writeLong(mSize);
        dest.writeParcelable(mFileKey, flags);
    }
}
