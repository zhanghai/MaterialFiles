/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ParcelableException implements Parcelable {

    @Nullable
    private Exception mException;

    public ParcelableException() {}

    public void set(@NonNull Exception exception) {
        if (mException != null) {
            throw new IllegalStateException("Already has an exception: " + mException);
        }
        mException = exception;
    }

    public void throwIfNotNull() throws IOException {
        if (mException instanceof IOException) {
            throw (IOException) mException;
        } else if (mException instanceof RuntimeException) {
            throw (RuntimeException) mException;
        } else if (mException != null) {
            throw new AssertionError(mException);
        }
    }


    public static final Creator<ParcelableException> CREATOR =
            new Creator<ParcelableException>() {
                @Override
                public ParcelableException createFromParcel(Parcel source) {
                    return new ParcelableException(source);
                }
                @Override
                public ParcelableException[] newArray(int size) {
                    return new ParcelableException[size];
                }
            };

    protected ParcelableException(Parcel in) {
        mException = (Exception) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel src) {
        mException = (Exception) src.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mException);
    }
}
