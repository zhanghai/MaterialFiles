/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root;

import android.os.Parcelable;

import java.io.IOException;

import androidx.annotation.NonNull;
import java8.nio.file.FileStore;
import java8.nio.file.Path;
import java8.nio.file.attribute.FileAttributeView;
import java9.util.function.Function;
import me.zhanghai.android.files.provider.common.PosixFileStore;

public abstract class RootablePosixFileStore extends PosixFileStore implements Parcelable {

    @NonNull
    private final Path mPath;
    @NonNull
    private final PosixFileStore mLocalFileStore;
    @NonNull
    private final RootPosixFileStore mRootFileStore;

    public RootablePosixFileStore(
            @NonNull Path path, @NonNull PosixFileStore localFileStore,
            @NonNull Function<PosixFileStore, RootPosixFileStore> newRootFileStore) {
        mPath = path;
        mLocalFileStore = localFileStore;
        mRootFileStore = newRootFileStore.apply(this);
    }

    @Override
    public void refresh() throws IOException {
        mLocalFileStore.refresh();
    }

    @NonNull
    @Override
    public String name() {
        return mLocalFileStore.name();
    }

    @NonNull
    @Override
    public String type() {
        return mLocalFileStore.type();
    }

    @Override
    public boolean isReadOnly() {
        return mLocalFileStore.isReadOnly();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws IOException {
        acceptRootable(mPath, storeFile -> {
            storeFile.setReadOnly(readOnly);
            if (storeFile == mRootFileStore) {
                mLocalFileStore.refresh();
            }
        });
    }

    @Override
    public long getTotalSpace() throws IOException {
        return applyRootable(mPath, FileStore::getTotalSpace);
    }

    @Override
    public long getUsableSpace() throws IOException {
        return applyRootable(mPath, FileStore::getUsableSpace);
    }

    @Override
    public long getUnallocatedSpace() throws IOException {
        return applyRootable(mPath, FileStore::getUnallocatedSpace);
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull Class<? extends FileAttributeView> type) {
        return mLocalFileStore.supportsFileAttributeView(type);
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull String name) {
        return mLocalFileStore.supportsFileAttributeView(name);
    }

    private void acceptRootable(@NonNull Path path, RootUtils.Consumer<PosixFileStore> consumer)
            throws IOException {
        RootUtils.acceptRootable(path, mLocalFileStore, mRootFileStore, consumer);
    }

    private <R> R applyRootable(@NonNull Path path, RootUtils.Function<PosixFileStore, R> function)
            throws IOException {
        return RootUtils.applyRootable(path, mLocalFileStore, mRootFileStore, function);
    }
}
