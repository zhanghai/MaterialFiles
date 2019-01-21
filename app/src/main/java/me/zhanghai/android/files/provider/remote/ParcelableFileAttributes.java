/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import java8.nio.file.attribute.BasicFileAttributes;

public class ParcelableFileAttributes implements Parcelable {

    @Nullable
    private final BasicFileAttributes mFileAttributes;

    public ParcelableFileAttributes(@Nullable BasicFileAttributes fileAttributes) {
        mFileAttributes = fileAttributes;
    }

    @Nullable
    public <FA extends BasicFileAttributes> FA get() {
        //noinspection unchecked
        return (FA) mFileAttributes;
    }


    public static final Creator<ParcelableFileAttributes> CREATOR =
            new Creator<ParcelableFileAttributes>() {
                @Override
                public ParcelableFileAttributes createFromParcel(Parcel source) {
                    return new ParcelableFileAttributes(source);
                }
                @Override
                public ParcelableFileAttributes[] newArray(int size) {
                    return new ParcelableFileAttributes[size];
                }
            };

    protected ParcelableFileAttributes(Parcel in) {
        mFileAttributes = in.readParcelable(BasicFileAttributes.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) mFileAttributes, flags);
    }
}
