/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class BaseFile implements File {

    @NonNull
    protected Uri mUri;

    public BaseFile(@NonNull Uri uri) {
        mUri = uri;
    }

    @NonNull
    public Uri getUri() {
        return mUri;
    }


    protected BaseFile(@NonNull Parcel in) {
        mUri = in.readParcelable(Uri.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(mUri, flags);
    }

    @Override
    public boolean equals(@Nullable Object object) {
        return equalsAsFile(object);
    }

    @Override
    public int hashCode() {
        return hashCodeAsFile();
    }
}
