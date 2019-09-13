/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.security.Principal;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class PosixPrincipal implements Parcelable, Principal {

    private final int mId;

    @Nullable
    private final ByteString mName;

    public PosixPrincipal(int id, @Nullable ByteString name) {
        mId = id;
        mName = name;
    }

    public int getId() {
        return mId;
    }

    @Nullable
    @Override
    public String getName() {
        return mName != null ? mName.toString() : null;
    }

    @Nullable
    public ByteString getNameByteString() {
        return mName;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        PosixPrincipal posixGroup = (PosixPrincipal) object;
        return mId == posixGroup.mId
                && Objects.equals(mName, posixGroup.mName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId, mName);
    }


    protected PosixPrincipal(@NonNull Parcel in) {
        mId = in.readInt();
        mName = in.readParcelable(ByteString.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeParcelable(mName, flags);
    }
}
