/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import androidx.annotation.Nullable;

public class SerializableObject implements Parcelable {

    @Nullable
    private final Object mObject;

    public SerializableObject(@Nullable Object object) {
        mObject = object;
    }

    @Nullable
    public <T> T get() {
        //noinspection unchecked
        return (T) mObject;
    }

    public static final Creator<SerializableObject> CREATOR =
            new Creator<SerializableObject>() {
                @Override
                public SerializableObject createFromParcel(Parcel source) {
                    return new SerializableObject(source);
                }
                @Override
                public SerializableObject[] newArray(int size) {
                    return new SerializableObject[size];
                }
            };

    protected SerializableObject(Parcel in) {
        mObject = in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable((Serializable) mObject);
    }
}
