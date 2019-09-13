/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.attribute.GroupPrincipal;

public class PosixGroup extends PosixPrincipal implements GroupPrincipal {

    public PosixGroup(int id, @Nullable ByteString name) {
        super(id, name);
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
        super(in);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }
}
