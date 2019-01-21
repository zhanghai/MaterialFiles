/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import java8.nio.file.FileSystem;

public class ParcelableFileSystem implements Parcelable {

    @Nullable
    private final FileSystem mFileSystem;

    public ParcelableFileSystem(@Nullable FileSystem fileSystem) {
        mFileSystem = fileSystem;
    }

    @Nullable
    public FileSystem get() {
        return mFileSystem;
    }

    public static final Creator<ParcelableFileSystem> CREATOR =
            new Creator<ParcelableFileSystem>() {
                @Override
                public ParcelableFileSystem createFromParcel(Parcel source) {
                    return new ParcelableFileSystem(source);
                }
                @Override
                public ParcelableFileSystem[] newArray(int size) {
                    return new ParcelableFileSystem[size];
                }
            };

    protected ParcelableFileSystem(Parcel in) {
        mFileSystem = in.readParcelable(FileSystem.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) mFileSystem, flags);
    }
}
