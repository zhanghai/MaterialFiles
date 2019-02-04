/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.os.Parcel;

import androidx.annotation.NonNull;
import java8.nio.file.attribute.FileAttributeView;
import me.zhanghai.android.files.provider.root.RootableFileStore;

class LinuxFileStore extends RootableFileStore {

    public LinuxFileStore(@NonNull String path) {
        super(new LocalLinuxFileStore(path));
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull Class<? extends FileAttributeView> type) {
        return LocalLinuxFileStore.supportsFileAttributeView_(type);
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull String name) {
        return LocalLinuxFileStore.supportsFileAttributeView_(name);
    }


    public static final Creator<LinuxFileStore> CREATOR = new Creator<LinuxFileStore>() {
        @Override
        public LinuxFileStore createFromParcel(Parcel source) {
            return new LinuxFileStore(source);
        }
        @Override
        public LinuxFileStore[] newArray(int size) {
            return new LinuxFileStore[size];
        }
    };

    protected LinuxFileStore(Parcel in) {
        super(in);
    }
}
