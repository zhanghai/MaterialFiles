/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.root.RootPosixFileStore;
import me.zhanghai.android.files.provider.root.RootablePosixFileStore;

class ArchiveFileStore extends RootablePosixFileStore {

    @NonNull
    private final Path mArchiveFile;

    public ArchiveFileStore(@NonNull Path archiveFile) {
        super(archiveFile, new LocalArchiveFileStore(archiveFile), RootPosixFileStore::new);

        mArchiveFile = archiveFile;
    }


    public static final Creator<ArchiveFileStore> CREATOR = new Creator<ArchiveFileStore>() {
        @Override
        public ArchiveFileStore createFromParcel(Parcel source) {
            return new ArchiveFileStore(source);
        }
        @Override
        public ArchiveFileStore[] newArray(int size) {
            return new ArchiveFileStore[size];
        }
    };

    protected ArchiveFileStore(Parcel in) {
        this((Path) in.readParcelable(Path.class.getClassLoader()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) mArchiveFile, flags);
    }
}
