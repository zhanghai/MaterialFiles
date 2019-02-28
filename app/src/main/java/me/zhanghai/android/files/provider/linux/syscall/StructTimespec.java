/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux.syscall;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @see android.system.StructTimespec
 */
public final class StructTimespec implements Parcelable {

    public final long tv_sec; /*time_t*/
    public final long tv_nsec;

    public StructTimespec(long tv_sec, long tv_nsec) {
        this.tv_sec = tv_sec;
        this.tv_nsec = tv_nsec;
    }


    public static final Creator<StructTimespec> CREATOR = new Creator<StructTimespec>() {
        @Override
        public StructTimespec createFromParcel(Parcel source) {
            return new StructTimespec(source);
        }
        @Override
        public StructTimespec[] newArray(int size) {
            return new StructTimespec[size];
        }
    };

    protected StructTimespec(Parcel in) {
        tv_sec = in.readLong();
        tv_nsec = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(tv_sec);
        dest.writeLong(tv_nsec);
    }
}
