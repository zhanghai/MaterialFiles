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

public class ParcelableIoException implements Parcelable {

    @Nullable
    private IOException mIoException;

    public ParcelableIoException() {}

    public void set(@NonNull IOException ioException) {
        if (mIoException != null) {
            throw new IllegalStateException("Already has an IOException: " + mIoException);
        }
        mIoException = ioException;
    }

    public void throwIfNotNull() throws IOException {
        if (mIoException != null) {
            throw mIoException;
        }
    }


    public static final Creator<ParcelableIoException> CREATOR =
            new Creator<ParcelableIoException>() {
                @Override
                public ParcelableIoException createFromParcel(Parcel source) {
                    return new ParcelableIoException(source);
                }
                @Override
                public ParcelableIoException[] newArray(int size) {
                    return new ParcelableIoException[size];
                }
            };

    protected ParcelableIoException(Parcel in) {
        mIoException = (IOException) in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel src) {
        mIoException = (IOException) src.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mIoException);
    }
}
