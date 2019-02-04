/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.Path;

class ArchiveFileKey implements Parcelable {

    @NonNull
    private final Path mArchiveFile;
    @NonNull
    private final String mEntryName;

    ArchiveFileKey(@NonNull Path archiveFile, @NonNull String entryName) {
        mArchiveFile = archiveFile;
        mEntryName = entryName;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ArchiveFileKey that = (ArchiveFileKey) object;
        return Objects.equals(mArchiveFile, that.mArchiveFile)
                && Objects.equals(mEntryName, that.mEntryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mArchiveFile, mEntryName);
    }


    public static final Creator<ArchiveFileKey> CREATOR = new Creator<ArchiveFileKey>() {
        @Override
        public ArchiveFileKey createFromParcel(Parcel source) {
            return new ArchiveFileKey(source);
        }
        @Override
        public ArchiveFileKey[] newArray(int size) {
            return new ArchiveFileKey[size];
        }
    };

    protected ArchiveFileKey(Parcel in) {
        mArchiveFile = in.readParcelable(Path.class.getClassLoader());
        mEntryName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) mArchiveFile, flags);
        dest.writeString(mEntryName);
    }
}
