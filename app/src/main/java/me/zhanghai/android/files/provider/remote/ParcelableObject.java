/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class ParcelableObject implements Parcelable {

    @Nullable
    private final Object mObject;

    public ParcelableObject(@Nullable Object object) {
        mObject = object;
    }

    @Nullable
    public <T extends Parcelable> T get() {
        //noinspection unchecked
        return (T) mObject;
    }


    public static final Creator<ParcelableObject> CREATOR =
            new Creator<ParcelableObject>() {
                @Override
                public ParcelableObject createFromParcel(Parcel source) {
                    return new ParcelableObject(source);
                }
                @Override
                public ParcelableObject[] newArray(int size) {
                    return new ParcelableObject[size];
                }
            };

    protected ParcelableObject(Parcel in) {
        mObject = in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) mObject, flags);
    }
}
