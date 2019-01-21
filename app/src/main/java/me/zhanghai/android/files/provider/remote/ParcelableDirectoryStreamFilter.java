/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import java8.nio.file.DirectoryStream;
import java8.nio.file.Path;

public class ParcelableDirectoryStreamFilter implements Parcelable {

    @Nullable
    private final DirectoryStream.Filter<? super Path> mFilter;

    public ParcelableDirectoryStreamFilter(@Nullable DirectoryStream.Filter<? super Path> filter) {
        mFilter = filter;
    }

    @Nullable
    public DirectoryStream.Filter<? super Path> get() {
        return mFilter;
    }

    public static final Creator<ParcelableDirectoryStreamFilter> CREATOR =
            new Creator<ParcelableDirectoryStreamFilter>() {
                @Override
                public ParcelableDirectoryStreamFilter createFromParcel(Parcel source) {
                    return new ParcelableDirectoryStreamFilter(source);
                }
                @Override
                public ParcelableDirectoryStreamFilter[] newArray(int size) {
                    return new ParcelableDirectoryStreamFilter[size];
                }
            };

    protected ParcelableDirectoryStreamFilter(Parcel in) {
        mFilter = in.readParcelable(DirectoryStream.Filter.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) mFilter, flags);
    }
}
