/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.os.Parcel;
import android.os.Parcelable;

import org.threeten.bp.Instant;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.attribute.FileTime;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.PosixFileAttributes;
import me.zhanghai.android.files.provider.common.PosixFileMode;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.provider.common.PosixFileType;
import me.zhanghai.android.files.provider.common.PosixFileTypes;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixUser;
import me.zhanghai.android.files.provider.linux.syscall.StructStat;

public class LinuxFileAttributes implements Parcelable, PosixFileAttributes {

    @NonNull
    private final StructStat mStat;
    @NonNull
    private final PosixUser mOwner;
    @NonNull
    private final PosixGroup mGroup;
    @Nullable
    private final ByteString mSeLinuxContext;

    LinuxFileAttributes(@NonNull StructStat stat, @NonNull PosixUser owner,
                        @NonNull PosixGroup group, @Nullable ByteString seLinuxContext) {
        mStat = stat;
        mOwner = owner;
        mGroup = group;
        mSeLinuxContext = seLinuxContext;
    }

    @NonNull
    @Override
    public FileTime lastModifiedTime() {
        return FileTime.from(Instant.ofEpochSecond(mStat.st_mtim.tv_sec, mStat.st_mtim.tv_nsec));
    }

    @NonNull
    @Override
    public FileTime lastAccessTime() {
        return FileTime.from(Instant.ofEpochSecond(mStat.st_atim.tv_sec, mStat.st_atim.tv_nsec));
    }

    @NonNull
    @Override
    public FileTime creationTime() {
        return lastModifiedTime();
    }

    @NonNull
    public PosixFileType type() {
        return PosixFileTypes.fromMode(mStat.st_mode);
    }

    @Override
    public long size() {
        return mStat.st_size;
    }

    @NonNull
    @Override
    public Parcelable fileKey() {
        return new LinuxFileKey(mStat.st_dev, mStat.st_ino);
    }

    @NonNull
    @Override
    public PosixUser owner() {
        return mOwner;
    }

    @NonNull
    @Override
    public PosixGroup group() {
        return mGroup;
    }

    @NonNull
    public Set<PosixFileModeBit> mode() {
        return PosixFileMode.fromInt(mStat.st_mode);
    }

    @Nullable
    @Override
    public ByteString seLinuxContext() {
        return mSeLinuxContext;
    }

    public static final Parcelable.Creator<LinuxFileAttributes> CREATOR =
            new Parcelable.Creator<LinuxFileAttributes>() {
                @Override
                public LinuxFileAttributes createFromParcel(Parcel source) {
                    return new LinuxFileAttributes(source);
                }
                @Override
                public LinuxFileAttributes[] newArray(int size) {
                    return new LinuxFileAttributes[size];
                }
            };

    protected LinuxFileAttributes(Parcel in) {
        mStat = in.readParcelable(StructStat.class.getClassLoader());
        mOwner = in.readParcelable(PosixUser.class.getClassLoader());
        mGroup = in.readParcelable(PosixGroup.class.getClassLoader());
        mSeLinuxContext = in.readParcelable(ByteString.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mStat, flags);
        dest.writeParcelable(mOwner, flags);
        dest.writeParcelable(mGroup, flags);
        dest.writeParcelable(mSeLinuxContext, flags);
    }
}
