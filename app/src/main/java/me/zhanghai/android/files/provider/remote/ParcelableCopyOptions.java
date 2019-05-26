/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import androidx.annotation.Nullable;
import java8.nio.file.CopyOption;

public class ParcelableCopyOptions implements Parcelable {

    @Nullable
    private CopyOption[] mCopyOptions;

    public ParcelableCopyOptions(@Nullable CopyOption[] copyOptions) {
        mCopyOptions = copyOptions;
    }

    @Nullable
    public CopyOption[] get() {
        return mCopyOptions;
    }


    public static final Creator<ParcelableCopyOptions> CREATOR =
            new Creator<ParcelableCopyOptions>() {
                @Override
                public ParcelableCopyOptions createFromParcel(Parcel source) {
                    return new ParcelableCopyOptions(source);
                }
                @Override
                public ParcelableCopyOptions[] newArray(int size) {
                    return new ParcelableCopyOptions[size];
                }
            };

    protected ParcelableCopyOptions(Parcel in) {
        int length = in.readInt();
        if (length == -1) {
            return;
        }
        mCopyOptions = new CopyOption[length];
        for (int i = 0; i < length; ++i) {
            byte type = in.readByte();
            switch (type) {
                case 0:
                    mCopyOptions[i] = in.readParcelable(getClass().getClassLoader());
                    break;
                case 1:
                    mCopyOptions[i] = (CopyOption) in.readSerializable();
                    break;
                default:
                    throw new AssertionError("type " + type);
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (mCopyOptions == null) {
            dest.writeInt(-1);
            return;
        }
        dest.writeInt(mCopyOptions.length);
        for (CopyOption option : mCopyOptions) {
            if (option instanceof Parcelable) {
                dest.writeByte((byte) 0);
                dest.writeParcelable((Parcelable) option, flags);
            } else if (option instanceof Serializable) {
                dest.writeByte((byte) 1);
                dest.writeSerializable((Serializable) option);
            } else {
                throw new UnsupportedOperationException(option.toString());
            }
        }
    }
}
