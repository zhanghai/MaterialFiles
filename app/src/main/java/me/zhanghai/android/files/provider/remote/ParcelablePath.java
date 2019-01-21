/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import java8.nio.file.Path;

public class ParcelablePath implements Parcelable {

    @NonNull
    private final Path mPath;

    public ParcelablePath(@NonNull Path path) {
        mPath = path;
    }

    @NonNull
    public Path get() {
        return mPath;
    }


    public static final Creator<ParcelablePath> CREATOR = new Creator<ParcelablePath>() {
        @Override
        public ParcelablePath createFromParcel(Parcel source) {
            return new ParcelablePath(source);
        }
        @Override
        public ParcelablePath[] newArray(int size) {
            return new ParcelablePath[size];
        }
    };

    protected ParcelablePath(Parcel in) {
        mPath = in.readParcelable(Path.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) mPath, flags);
    }
}
