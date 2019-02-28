/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.os.Parcel;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.root.RootFileSystem;
import me.zhanghai.android.files.provider.root.RootableFileSystem;

class LinuxFileSystem extends RootableFileSystem {

    static final char SEPARATOR = LocalLinuxFileSystem.SEPARATOR;

    public LinuxFileSystem(@NonNull LinuxFileSystemProvider provider) {
        super(fileSystem -> new LocalLinuxFileSystem((LinuxFileSystem) fileSystem, provider),
                RootFileSystem::new);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    protected LocalLinuxFileSystem getLocalFileSystem() {
        return super.getLocalFileSystem();
    }

    @NonNull
    Path getRootDirectory() {
        return getLocalFileSystem().getRootDirectory();
    }

    @NonNull
    Path getDefaultDirectory() {
        return getLocalFileSystem().getDefaultDirectory();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }


    public static final Creator<LinuxFileSystem> CREATOR = new Creator<LinuxFileSystem>() {
        @Override
        public LinuxFileSystem createFromParcel(Parcel source) {
            return LinuxFileSystemProvider.getFileSystem();
        }
        @Override
        public LinuxFileSystem[] newArray(int size) {
            return new LinuxFileSystem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {}
}
