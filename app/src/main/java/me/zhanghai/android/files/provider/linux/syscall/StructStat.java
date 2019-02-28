/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * @see android.system.StructStat
 */
public final class StructStat implements Parcelable {

    public final long st_dev; /*dev_t*/
    public final long st_ino; /*ino_t*/
    public final int st_mode; /*mode_t*/
    public final long st_nlink; /*nlink_t*/
    public final int st_uid; /*uid_t*/
    public final int st_gid; /*gid_t*/
    public final long st_rdev; /*dev_t*/
    public final long st_size; /*off_t*/
    public final long st_blksize; /*blksize_t*/
    public final long st_blocks; /*blkcnt_t*/
    @NonNull
    public final StructTimespec st_atim;
    @NonNull
    public final StructTimespec st_mtim;
    @NonNull
    public final StructTimespec st_ctim;
    public final long st_atime; /*time_t*/
    public final long st_mtime; /*time_t*/
    public final long st_ctime; /*time_t*/

    public StructStat(long st_dev, long st_ino, int st_mode, long st_nlink, int st_uid, int st_gid,
                      long st_rdev, long st_size, long st_blksize, long st_blocks,
                      @NonNull StructTimespec st_atim, @NonNull StructTimespec st_mtim,
                      @NonNull StructTimespec st_ctim) {
        this.st_dev = st_dev;
        this.st_ino = st_ino;
        this.st_mode = st_mode;
        this.st_nlink = st_nlink;
        this.st_uid = st_uid;
        this.st_gid = st_gid;
        this.st_rdev = st_rdev;
        this.st_size = st_size;
        this.st_blksize = st_blksize;
        this.st_blocks = st_blocks;
        this.st_atim = st_atim;
        this.st_mtim = st_mtim;
        this.st_ctim = st_ctim;
        this.st_atime = st_atim.tv_sec;
        this.st_mtime = st_mtim.tv_sec;
        this.st_ctime = st_ctim.tv_sec;
    }


    public static final Parcelable.Creator<StructStat> CREATOR =
            new Parcelable.Creator<StructStat>() {
                @Override
                public StructStat createFromParcel(Parcel source) {
                    return new StructStat(source);
                }
                @Override
                public StructStat[] newArray(int size) {
                    return new StructStat[size];
                }
            };

    protected StructStat(Parcel in) {
        st_dev = in.readLong();
        st_ino = in.readLong();
        st_mode = in.readInt();
        st_nlink = in.readLong();
        st_uid = in.readInt();
        st_gid = in.readInt();
        st_rdev = in.readLong();
        st_size = in.readLong();
        st_blksize = in.readLong();
        st_blocks = in.readLong();
        st_atim = in.readParcelable(StructTimespec.class.getClassLoader());
        st_mtim = in.readParcelable(StructTimespec.class.getClassLoader());
        st_ctim = in.readParcelable(StructTimespec.class.getClassLoader());
        st_atime = in.readLong();
        st_mtime = in.readLong();
        st_ctime = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(st_dev);
        dest.writeLong(st_ino);
        dest.writeInt(st_mode);
        dest.writeLong(st_nlink);
        dest.writeInt(st_uid);
        dest.writeInt(st_gid);
        dest.writeLong(st_rdev);
        dest.writeLong(st_size);
        dest.writeLong(st_blksize);
        dest.writeLong(st_blocks);
        dest.writeParcelable(st_atim, flags);
        dest.writeParcelable(st_mtim, flags);
        dest.writeParcelable(st_ctim, flags);
        dest.writeLong(st_atime);
        dest.writeLong(st_mtime);
        dest.writeLong(st_ctime);
    }
}
