/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.attribute.GroupPrincipal;

public class PosixGroup implements Parcelable, GroupPrincipal {

    private final int mId;

    @Nullable
    private final String mName;

    public PosixGroup(int id, @Nullable String name) {
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
        PosixGroup posixGroup = (PosixGroup) object;
        return mId == posixGroup.mId
                && Objects.equals(mName, posixGroup.mName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId, mName);
    }


    public static final Creator<PosixGroup> CREATOR = new Creator<PosixGroup>() {
        @NonNull
        @Override
        public PosixGroup createFromParcel(@NonNull Parcel source) {
            return new PosixGroup(source);
        }
        @NonNull
        @Override
        public PosixGroup[] newArray(int size) {
            return new PosixGroup[size];
        }
    };

    protected PosixGroup(@NonNull Parcel in) {
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
