/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import androidx.annotation.Nullable;

public class ParcelableSerializable implements Parcelable {

    @Nullable
    private final Serializable mSerializable;

    public ParcelableSerializable(@Nullable Serializable serializable) {
        mSerializable = serializable;
    }

    @Nullable
    public <T extends Serializable> T get() {
        //noinspection unchecked
        return (T) mSerializable;
    }


    public static final Creator<ParcelableSerializable> CREATOR =
            new Creator<ParcelableSerializable>() {
                @Override
                public ParcelableSerializable createFromParcel(Parcel source) {
                    return new ParcelableSerializable(source);
                }
                @Override
                public ParcelableSerializable[] newArray(int size) {
                    return new ParcelableSerializable[size];
                }
            };

    protected ParcelableSerializable(Parcel in) {
        mSerializable = in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mSerializable);
    }
}
