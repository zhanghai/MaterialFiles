/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import me.zhanghai.android.files.filesystem.Files;

public class StandardDirectory implements Parcelable {

    @DrawableRes
    private int mIconRes;

    @StringRes
    private int mTitleRes;

    @Nullable
    private String mTitle;

    @NonNull
    private String mRelativePath;

    private boolean mEnabled;

    public StandardDirectory(@DrawableRes int iconRes, @StringRes int titleRes,
                             @Nullable String title, @NonNull String relativePath,
                             boolean enabled) {
        mIconRes = iconRes;
        mTitleRes = titleRes;
        mTitle = title;
        mRelativePath = relativePath;
        mEnabled = enabled;
    }

    @DrawableRes
    public int getIconRes() {
        return mIconRes;
    }

    @NonNull
    public String getTitle(@NonNull Context context) {
        if (!TextUtils.isEmpty(mTitle)) {
            return mTitle;
        }
        if (mTitleRes != 0) {
            return context.getString(mTitleRes);
        }
        return Files.ofLocalPath(mRelativePath).getName();
    }

    public void setTitle(@Nullable String title) {
        mTitle = title;
    }

    @NonNull
    public String getRelativePath() {
        return mRelativePath;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }


    public static final Creator<StandardDirectory> CREATOR =
            new Creator<StandardDirectory>() {
                @Override
                public StandardDirectory createFromParcel(Parcel source) {
                    return new StandardDirectory(source);
                }
                @Override
                public StandardDirectory[] newArray(int size) {
                    return new StandardDirectory[size];
                }
            };

    protected StandardDirectory(Parcel in) {
        mIconRes = in.readInt();
        mTitleRes = in.readInt();
        mTitle = in.readString();
        mRelativePath = in.readString();
        mEnabled = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mIconRes);
        dest.writeInt(mTitleRes);
        dest.writeString(mTitle);
        dest.writeString(mRelativePath);
        dest.writeByte(mEnabled ? (byte) 1 : (byte) 0);
    }
}
