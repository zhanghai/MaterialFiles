/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StandardDirectorySettings implements Parcelable {

    @NonNull
    private final String mId;

    @Nullable
    private final String mTitle;

    private final boolean mEnabled;

    public StandardDirectorySettings(@NonNull String id, @Nullable String title, boolean enabled) {
        mId = id;
        mTitle = title;
        mEnabled = enabled;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    @NonNull
    public StandardDirectorySettings withEnabled(boolean enabled) {
        if (mEnabled == enabled) {
            return this;
        }
        return new StandardDirectorySettings(mId, mTitle, enabled);
    }


    public static final Creator<StandardDirectorySettings> CREATOR =
            new Creator<StandardDirectorySettings>() {
                @Override
                public StandardDirectorySettings createFromParcel(Parcel source) {
                    return new StandardDirectorySettings(source);
                }
                @Override
                public StandardDirectorySettings[] newArray(int size) {
                    return new StandardDirectorySettings[size];
                }
            };

    protected StandardDirectorySettings(Parcel in) {
        mId = in.readString();
        mTitle = in.readString();
        mEnabled = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mTitle);
        dest.writeByte(mEnabled ? (byte) 1 : (byte) 0);
    }
}
