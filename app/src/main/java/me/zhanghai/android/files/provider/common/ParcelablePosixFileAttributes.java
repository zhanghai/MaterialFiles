/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.attribute.FileTime;

public class ParcelablePosixFileAttributes implements Parcelable, PosixFileAttributes {

    @NonNull
    private final FileTime mLastModifiedTime;
    @NonNull
    private final FileTime mLastAccessTime;
    @NonNull
    private final FileTime mCreationTime;
    @NonNull
    private final PosixFileType mType;
    private final long mSize;
    @NonNull
    private final Parcelable mFileKey;
    @Nullable
    private final PosixUser mOwner;
    @Nullable
    private final PosixGroup mGroup;
    @Nullable
    private final Set<PosixFileModeBit> mMode;
    @Nullable
    private final ByteString mSeLinuxContext;

    public ParcelablePosixFileAttributes(@NonNull PosixFileAttributes attributes) {
        mLastModifiedTime = attributes.lastModifiedTime();
        mLastAccessTime = attributes.lastAccessTime();
        mCreationTime = attributes.creationTime();
        mType = attributes.type();
        mSize = attributes.size();
        mFileKey = attributes.fileKey();
        mOwner = attributes.owner();
        mGroup = attributes.group();
        mMode = attributes.mode();
        mSeLinuxContext = attributes.seLinuxContext();
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

    @NonNull
    public PosixFileType type() {
        return mType;
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

    @Override
    @Nullable
    public PosixUser owner() {
        return mOwner;
    }

    @Override
    @Nullable
    public PosixGroup group() {
        return mGroup;
    }

    @Nullable
    public Set<PosixFileModeBit> mode() {
        return mMode;
    }

    @Nullable
    @Override
    public ByteString seLinuxContext() {
        return mSeLinuxContext;
    }


    public static final Creator<ParcelablePosixFileAttributes> CREATOR =
            new Creator<ParcelablePosixFileAttributes>() {
                @Override
                public ParcelablePosixFileAttributes createFromParcel(Parcel source) {
                    return new ParcelablePosixFileAttributes(source);
                }
                @Override
                public ParcelablePosixFileAttributes[] newArray(int size) {
                    return new ParcelablePosixFileAttributes[size];
                }
            };

    protected ParcelablePosixFileAttributes(Parcel in) {
        mLastModifiedTime = ((ParcelableFileTime) in.readParcelable(
                ParcelableFileTime.class.getClassLoader())).get();
        mLastAccessTime = ((ParcelableFileTime) in.readParcelable(
                ParcelableFileTime.class.getClassLoader())).get();
        mCreationTime = ((ParcelableFileTime) in.readParcelable(
                ParcelableFileTime.class.getClassLoader())).get();
        int tmpMType = in.readInt();
        mType = tmpMType == -1 ? null : PosixFileType.values()[tmpMType];
        mSize = in.readLong();
        mFileKey = in.readParcelable(getClass().getClassLoader());
        mOwner = in.readParcelable(PosixUser.class.getClassLoader());
        mGroup = in.readParcelable(PosixGroup.class.getClassLoader());
        mMode = ((ParcelablePosixFileMode) in.readParcelable(
                ParcelablePosixFileMode.class.getClassLoader())).get();
        mSeLinuxContext = in.readParcelable(ByteString.class.getClassLoader());
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
        dest.writeInt(mType == null ? -1 : mType.ordinal());
        dest.writeLong(mSize);
        dest.writeParcelable(mFileKey, flags);
        dest.writeParcelable(mOwner, flags);
        dest.writeParcelable(mGroup, flags);
        dest.writeParcelable(new ParcelablePosixFileMode(mMode), flags);
        dest.writeParcelable(mSeLinuxContext, flags);
    }
}
