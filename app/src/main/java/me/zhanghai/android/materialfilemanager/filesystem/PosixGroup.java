/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class PosixGroup implements Parcelable {

    public int id;

    public String name;


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        PosixGroup posixUser = (PosixGroup) object;
        return id == posixUser.id
                && Objects.equals(name, posixUser.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }


    public static final Creator<PosixGroup> CREATOR = new Creator<PosixGroup>() {
        @Override
        public PosixGroup createFromParcel(Parcel source) {
            return new PosixGroup(source);
        }
        @Override
        public PosixGroup[] newArray(int size) {
            return new PosixGroup[size];
        }
    };

    public PosixGroup() {}

    protected PosixGroup(Parcel in) {
        id = in.readInt();
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
    }
}
