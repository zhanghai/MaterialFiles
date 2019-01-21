/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class ParcelableClass implements Parcelable {

    @Nullable
    private final Class<?> mClass;

    public ParcelableClass(@Nullable Class<?> clazz) {
        mClass = clazz;
    }

    @Nullable
    public <T> Class<T> get() {
        //noinspection unchecked
        return (Class<T>) mClass;
    }


    public static final Creator<ParcelableClass> CREATOR = new Creator<ParcelableClass>() {
        @Override
        public ParcelableClass createFromParcel(Parcel source) {
            return new ParcelableClass(source);
        }
        @Override
        public ParcelableClass[] newArray(int size) {
            return new ParcelableClass[size];
        }
    };

    protected ParcelableClass(Parcel in) {
        mClass = (Class<?>) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mClass);
    }
}
