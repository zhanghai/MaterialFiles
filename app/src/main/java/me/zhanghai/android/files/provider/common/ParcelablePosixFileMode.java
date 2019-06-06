/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;

import androidx.annotation.Nullable;

public class ParcelablePosixFileMode implements Parcelable {

    @Nullable
    private final Set<PosixFileModeBit> mPosixFileMode;

    public ParcelablePosixFileMode(@Nullable Set<PosixFileModeBit> posixFileMode) {
        mPosixFileMode = posixFileMode;
    }

    @Nullable
    public Set<PosixFileModeBit> get() {
        return mPosixFileMode;
    }


    public static final Creator<ParcelablePosixFileMode> CREATOR =
            new Creator<ParcelablePosixFileMode>() {
                @Override
                public ParcelablePosixFileMode createFromParcel(Parcel source) {
                    return new ParcelablePosixFileMode(source);
                }
                @Override
                public ParcelablePosixFileMode[] newArray(int size) {
                    return new ParcelablePosixFileMode[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Serializable serializable;
        if (mPosixFileMode instanceof Serializable) {
            serializable = (Serializable) mPosixFileMode;
        } else if (mPosixFileMode == null) {
            serializable = null;
        } else {
            EnumSet<PosixFileModeBit> posixFileMode = EnumSet.noneOf(PosixFileModeBit.class);
            posixFileMode.addAll(mPosixFileMode);
            serializable = posixFileMode;
        }
        dest.writeSerializable(serializable);
    }

    protected ParcelablePosixFileMode(Parcel in) {
        //noinspection unchecked
        mPosixFileMode = (Set<PosixFileModeBit>) in.readSerializable();
    }
}
