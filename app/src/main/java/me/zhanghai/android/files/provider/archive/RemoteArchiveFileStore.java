/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import android.os.Parcel;

import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.FileStore;
import java8.nio.file.attribute.FileAttributeView;
import me.zhanghai.android.files.provider.remote.RemoteFileStore;

public class RemoteArchiveFileStore extends RemoteFileStore {

    RemoteArchiveFileStore(@NonNull FileStore fileStore) {
        super(fileStore);
    }

    @Override
    public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
        Objects.requireNonNull(type);
        return RemoteArchiveFileSystemProvider.supportsFileAttributeView(type);
    }

    @Override
    public boolean supportsFileAttributeView(String name) {
        Objects.requireNonNull(name);
        return RemoteArchiveFileAttributeView.SUPPORTED_NAMES.contains(name);
    }


    public static final Creator<RemoteArchiveFileStore> CREATOR =
            new Creator<RemoteArchiveFileStore>() {
                @Override
                public RemoteArchiveFileStore createFromParcel(Parcel source) {
                    return new RemoteArchiveFileStore(source);
                }
                @Override
                public RemoteArchiveFileStore[] newArray(int size) {
                    return new RemoteArchiveFileStore[size];
                }
            };

    protected RemoteArchiveFileStore(Parcel in) {
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
