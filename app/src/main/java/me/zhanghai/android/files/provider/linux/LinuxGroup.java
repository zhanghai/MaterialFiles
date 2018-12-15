/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.attribute.GroupPrincipal;

public class LinuxGroup implements Parcelable, GroupPrincipal {

    private final int mId;

    @Nullable
    private final String mName;

    LinuxGroup(int id, @Nullable String name) {
        mId = id;
        mName = name;
    }

    public int getId() {
        return mId;
    }

    @Nullable
    @Override
    public String getName() {
        return mName;
    }


    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        LinuxGroup posixUser = (LinuxGroup) object;
        return mId == posixUser.mId
                && Objects.equals(mName, posixUser.mName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId, mName);
    }


    public static final Creator<LinuxGroup> CREATOR = new Creator<LinuxGroup>() {
        @NonNull
        @Override
        public LinuxGroup createFromParcel(@NonNull Parcel source) {
            return new LinuxGroup(source);
        }
        @NonNull
        @Override
        public LinuxGroup[] newArray(int size) {
            return new LinuxGroup[size];
        }
    };

    protected LinuxGroup(@NonNull Parcel in) {
        mId = in.readInt();
        mName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mName);
    }
}
