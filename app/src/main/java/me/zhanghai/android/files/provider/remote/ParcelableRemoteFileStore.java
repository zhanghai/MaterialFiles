/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import java8.nio.file.FileSystem;

public class ParcelableRemoteFileStore implements Parcelable {

    @Nullable
    private final RemoteFileStore mFileStore;

    public ParcelableRemoteFileStore(@Nullable RemoteFileStore fileStore) {
        mFileStore = fileStore;
    }

    @Nullable
    public <RFS extends RemoteFileStore> RFS get() {
        //noinspection unchecked
        return (RFS) mFileStore;
    }

    public static final Creator<ParcelableRemoteFileStore> CREATOR =
            new Creator<ParcelableRemoteFileStore>() {
                @Override
                public ParcelableRemoteFileStore createFromParcel(Parcel source) {
                    return new ParcelableRemoteFileStore(source);
                }
                @Override
                public ParcelableRemoteFileStore[] newArray(int size) {
                    return new ParcelableRemoteFileStore[size];
                }
            };

    protected ParcelableRemoteFileStore(Parcel in) {
        mFileStore = in.readParcelable(FileSystem.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mFileStore, flags);
    }
}
