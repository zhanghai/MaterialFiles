/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import java.util.Objects;

import androidx.annotation.Nullable;

class LinuxFileKey {

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
}
