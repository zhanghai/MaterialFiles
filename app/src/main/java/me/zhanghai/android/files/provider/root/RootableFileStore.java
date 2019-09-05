/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;

import androidx.annotation.NonNull;
import me.zhanghai.android.files.provider.common.AbstractFileStore;

public abstract class RootableFileStore extends AbstractFileStore implements Parcelable {

    @NonNull
    private final AbstractFileStore mFileStore;

    public RootableFileStore(@NonNull AbstractFileStore fileStore) {
        mFileStore = fileStore;
    }

    @NonNull
    @Override
    public String name() {
        return mFileStore.name();
    }

    @NonNull
    @Override
    public String type() {
        return mFileStore.type();
    }

    @Override
    public boolean isReadOnly() {
        return mFileStore.isReadOnly();
    }

    @Override
    public long getTotalSpace() throws IOException {
        return mFileStore.getTotalSpace();
    }

    @Override
    public long getUsableSpace() throws IOException {
        return mFileStore.getUsableSpace();
    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        return mFileStore.getUnallocatedSpace();
    }


    protected RootableFileStore(Parcel in) {
        RootUtils.requireRunningAsNonRoot();
        mFileStore = in.readParcelable(RootFileStore.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        RootUtils.requireRunningAsRoot();
        RootFileStore fileStore = new RootFileStore(mFileStore);
        dest.writeParcelable(fileStore, flags);
    }
}
