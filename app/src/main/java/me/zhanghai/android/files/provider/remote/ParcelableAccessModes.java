/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import java8.nio.file.AccessMode;

public class ParcelableAccessModes implements Parcelable {

    @Nullable
    private AccessMode[] mAccessModes;

    public ParcelableAccessModes(@Nullable AccessMode[] accessModes) {
        mAccessModes = accessModes;
    }

    @Nullable
    public AccessMode[] get() {
        return mAccessModes;
    }


    public static final Creator<ParcelableAccessModes> CREATOR =
            new Creator<ParcelableAccessModes>() {
                @Override
                public ParcelableAccessModes createFromParcel(Parcel source) {
                    return new ParcelableAccessModes(source);
                }
                @Override
                public ParcelableAccessModes[] newArray(int size) {
                    return new ParcelableAccessModes[size];
                }
            };

    protected ParcelableAccessModes(Parcel in) {
        //noinspection unchecked
        List<AccessMode> accessModes = (List<AccessMode>) in.readSerializable();
        mAccessModes = accessModes != null ? accessModes.toArray(new AccessMode[0]) : null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mAccessModes != null ? (Serializable) Arrays.asList(mAccessModes)
                : null);
    }
}
