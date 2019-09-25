/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import java9.util.function.Consumer;
import me.zhanghai.android.files.util.BundleBuilder;
import me.zhanghai.android.files.util.BundleUtils;
import me.zhanghai.android.files.util.CollectionUtils;
import me.zhanghai.android.files.util.RemoteCallback;

public class ParcelablePathListConsumer implements Parcelable {

    private static final String KEY_PREFIX = ParcelablePathListConsumer.class.getName() + '.';

    private static final String KEY_PATH_LIST = KEY_PREFIX + "PATH_LIST";

    @NonNull
    private final Consumer<List<Path>> mListener;

    public ParcelablePathListConsumer(@NonNull Consumer<List<Path>> listener) {
        mListener = listener;
    }

    @NonNull
    public Consumer<List<Path>> get() {
        return mListener;
    }


    public static final Creator<ParcelablePathListConsumer> CREATOR =
            new Creator<ParcelablePathListConsumer>() {
                @Override
                public ParcelablePathListConsumer createFromParcel(Parcel source) {
                    return new ParcelablePathListConsumer(source);
                }
                @Override
                public ParcelablePathListConsumer[] newArray(int size) {
                    return new ParcelablePathListConsumer[size];
                }
            };

    protected ParcelablePathListConsumer(Parcel in) {
        RemoteCallback remoteCallback = in.readParcelable(RemoteCallback.class.getClassLoader());
        //noinspection unchecked
        mListener = paths -> remoteCallback.sendResult(new BundleBuilder()
                .putParcelableArrayList(KEY_PATH_LIST,
                        (ArrayList<Parcelable>) (ArrayList<?>) CollectionUtils.toArrayList(paths))
                .build());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //noinspection unchecked
        dest.writeParcelable(new RemoteCallback(result -> mListener.accept(
                (List<Path>) (List<?>) BundleUtils.getParcelableArrayList(result, KEY_PATH_LIST))),
                flags);
    }
}
