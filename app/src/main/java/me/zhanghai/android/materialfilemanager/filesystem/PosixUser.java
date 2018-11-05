/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PosixUser implements Parcelable {

    public int id;

    @Nullable
    public String name;


    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        PosixUser posixUser = (PosixUser) object;
        return id == posixUser.id
                && Objects.equals(name, posixUser.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
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

    public PosixUser() {}

    protected PosixUser(@NonNull Parcel in) {
        id = in.readInt();
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
    }
}
