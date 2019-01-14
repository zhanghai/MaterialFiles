/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

class LinuxFileKey implements Parcelable {

    private final long mDeviceId;
    private final long mInodeNumber;

    public LinuxFileKey(long deviceId, long inodeNumber) {
        mDeviceId = deviceId;
        mInodeNumber = inodeNumber;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        LinuxFileKey that = (LinuxFileKey) object;
        return mDeviceId == that.mDeviceId
                && mInodeNumber == that.mInodeNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mDeviceId, mInodeNumber);
    }


    public static final Creator<LinuxFileKey> CREATOR = new Creator<LinuxFileKey>() {
        @Override
        public LinuxFileKey createFromParcel(Parcel source) {
            return new LinuxFileKey(source);
        }
        @Override
        public LinuxFileKey[] newArray(int size) {
            return new LinuxFileKey[size];
        }
    };

    protected LinuxFileKey(Parcel in) {
        mDeviceId = in.readLong();
        mInodeNumber = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mDeviceId);
        dest.writeLong(mInodeNumber);
    }
}
