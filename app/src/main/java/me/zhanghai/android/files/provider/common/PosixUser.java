/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.attribute.UserPrincipal;

public class PosixUser extends PosixPrincipal implements UserPrincipal {

    public PosixUser(int id, @Nullable ByteString name) {
        super(id, name);
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
        super(in);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }
}
