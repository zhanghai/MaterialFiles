/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import android.os.Parcel;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import java8.nio.file.attribute.FileAttributeView;
import me.zhanghai.android.files.provider.root.RootableFileStore;

class ArchiveFileStore extends RootableFileStore {

    ArchiveFileStore(@NonNull Path archiveFile) {
        super(new LocalArchiveFileStore(archiveFile));
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull Class<? extends FileAttributeView> type) {
        return LocalArchiveFileStore.supportsFileAttributeView_(type);
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull String name) {
        return LocalArchiveFileStore.supportsFileAttributeView_(name);
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
        super(in);
    }
}
