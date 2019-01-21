/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import java8.nio.file.LinkOption;

public class ParcelableLinkOptions implements Parcelable {

    @Nullable
    private LinkOption[] mLinkOptions;

    public ParcelableLinkOptions(@Nullable LinkOption[] linkOptions) {
        mLinkOptions = linkOptions;
    }

    @Nullable
    public LinkOption[] get() {
        return mLinkOptions;
    }


    public static final Creator<ParcelableLinkOptions> CREATOR =
            new Creator<ParcelableLinkOptions>() {
                @Override
                public ParcelableLinkOptions createFromParcel(Parcel source) {
                    return new ParcelableLinkOptions(source);
                }
                @Override
                public ParcelableLinkOptions[] newArray(int size) {
                    return new ParcelableLinkOptions[size];
                }
            };

    protected ParcelableLinkOptions(Parcel in) {
        //noinspection unchecked
        List<LinkOption> linkOptions = (List<LinkOption>) in.readSerializable();
        mLinkOptions = linkOptions != null ? linkOptions.toArray(new LinkOption[0]) : null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mLinkOptions != null ? (Serializable) Arrays.asList(mLinkOptions)
                : null);
    }
}
