/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.os.Parcel;

import java.io.IOException;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.root.RootPosixFileStore;
import me.zhanghai.android.files.provider.root.RootablePosixFileStore;

class LinuxFileStore extends RootablePosixFileStore {

    @NonNull
    private final LinuxPath mPath;
    @NonNull
    private final LocalLinuxFileStore mLocalFileStore;

    public LinuxFileStore(@NonNull LinuxPath path) throws IOException {
        this(path, new LocalLinuxFileStore(path));
    }

    private LinuxFileStore(@NonNull LinuxPath path, @NonNull LocalLinuxFileStore localFileStore) {
        super(path, localFileStore, RootPosixFileStore::new);

        mPath = path;
        mLocalFileStore = localFileStore;
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
        this(in.readParcelable(Path.class.getClassLoader()),
                in.readParcelable(LocalLinuxFileStore.class.getClassLoader()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mPath, flags);
        dest.writeParcelable(mLocalFileStore, flags);
    }
}
