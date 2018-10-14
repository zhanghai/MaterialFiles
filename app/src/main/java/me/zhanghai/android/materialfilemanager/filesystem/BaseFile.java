/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filesystem;

import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.NonNull;

public abstract class BaseFile implements File {

    protected Uri mUri;

    public BaseFile(Uri uri) {
        mUri = uri;
    }

    @NonNull
    public Uri getUri() {
        return mUri;
    }


    protected BaseFile(Parcel in) {
        mUri = in.readParcelable(Uri.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mUri, flags);
    }

    @Override
    public boolean equals(Object object) {
        return equalsAsFile(object);
    }

    @Override
    public int hashCode() {
        return hashCodeAsFile();
    }
}
