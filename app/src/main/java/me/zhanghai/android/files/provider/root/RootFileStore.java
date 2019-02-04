/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root;

import android.os.Parcel;

import androidx.annotation.NonNull;
import java8.nio.file.attribute.FileAttributeView;
import me.zhanghai.android.files.provider.common.FileStore;
import me.zhanghai.android.files.provider.remote.RemoteFileStore;

public class RootFileStore extends RemoteFileStore {

    public RootFileStore(@NonNull FileStore fileStore) {
        super(fileStore);
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull Class<? extends FileAttributeView> type) {
        throw new AssertionError();
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull String name) {
        throw new AssertionError();
    }


    public static final Creator<RootFileStore> CREATOR = new Creator<RootFileStore>() {
        @Override
        public RootFileStore createFromParcel(Parcel source) {
            return new RootFileStore(source);
        }
        @Override
        public RootFileStore[] newArray(int size) {
            return new RootFileStore[size];
        }
    };

    protected RootFileStore(Parcel in) {
        super(in);
    }
}
