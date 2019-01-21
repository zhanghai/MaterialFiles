/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import java8.nio.file.FileSystem;
import java8.nio.file.attribute.FileAttributeView;

public class ParcelableFileAttributeView implements Parcelable {

    @Nullable
    private final FileAttributeView mAttributeView;

    public ParcelableFileAttributeView(@Nullable FileAttributeView attributeView) {
        mAttributeView = attributeView;
    }

    @Nullable
    public <FAV extends FileAttributeView> FAV get() {
        //noinspection unchecked
        return (FAV) mAttributeView;
    }

    public static final Creator<ParcelableFileAttributeView> CREATOR =
            new Creator<ParcelableFileAttributeView>() {
                @Override
                public ParcelableFileAttributeView createFromParcel(Parcel source) {
                    return new ParcelableFileAttributeView(source);
                }
                @Override
                public ParcelableFileAttributeView[] newArray(int size) {
                    return new ParcelableFileAttributeView[size];
                }
            };

    protected ParcelableFileAttributeView(Parcel in) {
        mAttributeView = in.readParcelable(FileSystem.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) mAttributeView, flags);
    }
}
