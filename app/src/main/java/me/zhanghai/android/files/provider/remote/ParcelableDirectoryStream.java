/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.DirectoryIteratorException;
import java8.nio.file.DirectoryStream;
import java8.nio.file.Path;
import me.zhanghai.android.files.functional.IterableCompat;
import me.zhanghai.android.files.provider.common.PathListDirectoryStream;

public class ParcelableDirectoryStream implements Parcelable {

    @Nullable
    private final List<Path> mPaths;

    private transient boolean mGetCalled;

    private ParcelableDirectoryStream(@Nullable List<Path> paths) {
        mPaths = paths;
    }

    @NonNull
    public static ParcelableDirectoryStream createForRemote(
            @Nullable DirectoryStream<Path> directoryStream) throws IOException {
        if (directoryStream == null) {
            return new ParcelableDirectoryStream((List<Path>) null);
        }
        List<Path> paths = new ArrayList<>();
        try {
            IterableCompat.forEach(directoryStream, path -> paths.add(RemoteUtils.toRemotePath(
                    path)));
        } catch (DirectoryIteratorException e) {
            throw e.getCause();
        }
        return new ParcelableDirectoryStream(paths);
    }

    @Nullable
    public DirectoryStream<Path> get() {
        if (mGetCalled) {
            throw new IllegalStateException("Already called get() on this instance");
        }
        mGetCalled = true;
        return mPaths != null ? new PathListDirectoryStream(mPaths, path -> true) : null;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(mPaths);
    }

    protected ParcelableDirectoryStream(Parcel in) {
        //noinspection unchecked
        mPaths = in.readArrayList(Path.class.getClassLoader());
    }

    public static final Creator<ParcelableDirectoryStream> CREATOR =
            new Creator<ParcelableDirectoryStream>() {
                @Override
                public ParcelableDirectoryStream createFromParcel(Parcel source) {
                    return new ParcelableDirectoryStream(source);
                }
                @Override
                public ParcelableDirectoryStream[] newArray(int size) {
                    return new ParcelableDirectoryStream[size];
                }
            };
}
