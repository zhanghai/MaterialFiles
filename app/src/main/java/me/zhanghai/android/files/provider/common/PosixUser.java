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
import java8.nio.file.attribute.UserPrincipal;

public class PosixUser implements Parcelable, UserPrincipal {

    private final int mId;

    @Nullable
    private final String mName;

    public PosixUser(int id, @Nullable String name) {
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
        PosixUser posixUser = (PosixUser) object;
        return mId == posixUser.mId
                && Objects.equals(mName, posixUser.mName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId, mName);
    }


    public static final Creator<PosixUser> CREATOR = new Creator<PosixUser>() {
        @NonNull
        @Override
        public PosixUser createFromParcel(@NonNull Parcel source) {
            return new PosixUser(source);
        }
        @NonNull
        @Override
        public PosixUser[] newArray(int size) {
            return new PosixUser[size];
        }
    };

    protected PosixUser(@NonNull Parcel in) {
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
