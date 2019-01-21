/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.os.Parcel;

import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.FileStore;
import java8.nio.file.attribute.FileAttributeView;
import me.zhanghai.android.files.provider.remote.RemoteFileStore;

public class RemoteLinuxFileStore extends RemoteFileStore {

    RemoteLinuxFileStore(@NonNull FileStore fileStore) {
        super(fileStore);
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        Objects.requireNonNull(type);
        return RemoteLinuxFileSystemProvider.supportsFileAttributeView(type);
    }

    @Override
    public boolean supportsFileAttributeView(String name) {
        Objects.requireNonNull(name);
        return RemoteLinuxFileAttributeView.SUPPORTED_NAMES.contains(name);
    }


    public static final Creator<RemoteLinuxFileStore> CREATOR =
            new Creator<RemoteLinuxFileStore>() {
                @Override
                public RemoteLinuxFileStore createFromParcel(Parcel source) {
                    return new RemoteLinuxFileStore(source);
                }
                @Override
                public RemoteLinuxFileStore[] newArray(int size) {
                    return new RemoteLinuxFileStore[size];
                }
            };

    protected RemoteLinuxFileStore(Parcel in) {
        super(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }
}
