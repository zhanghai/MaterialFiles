/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.os.Parcel;
import android.os.Parcelable;

import org.threeten.bp.Instant;

import androidx.annotation.Nullable;
import java8.nio.file.attribute.FileTime;

public class ParcelableFileTime implements Parcelable {

    @Nullable
    private final FileTime mFileTime;

    public ParcelableFileTime(@Nullable FileTime fileTime) {
        mFileTime = fileTime;
    }

    @Nullable
    public FileTime get() {
        return mFileTime;
    }


    public static final Creator<ParcelableFileTime> CREATOR = new Creator<ParcelableFileTime>() {
        @Override
        public ParcelableFileTime createFromParcel(Parcel source) {
            return new ParcelableFileTime(source);
        }
        @Override
        public ParcelableFileTime[] newArray(int size) {
            return new ParcelableFileTime[size];
        }
    };

    protected ParcelableFileTime(Parcel in) {
        Instant instant = (Instant) in.readSerializable();
        mFileTime = instant != null ? FileTime.from(instant) : null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mFileTime != null ? mFileTime.toInstant() : null);
    }
}
