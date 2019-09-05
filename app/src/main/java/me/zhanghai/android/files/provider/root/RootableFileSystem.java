/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root;

import android.os.Parcelable;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.file.FileStore;
import java8.nio.file.FileSystem;
import java8.nio.file.Path;
import java8.nio.file.PathMatcher;
import java8.nio.file.WatchService;
import java8.nio.file.attribute.UserPrincipalLookupService;
import java8.nio.file.spi.FileSystemProvider;
import java9.util.function.Function;
import me.zhanghai.android.files.provider.remote.RemoteFileSystemException;

public abstract class RootableFileSystem extends FileSystem implements Parcelable {

    @NonNull
    private final FileSystem mLocalFileSystem;
    @NonNull
    private final RootFileSystem mRootFileSystem;

    public RootableFileSystem(@NonNull Function<FileSystem, FileSystem> newLocalFileSystem,
                              @NonNull Function<FileSystem, RootFileSystem> newRootFileSystem) {
        mLocalFileSystem = newLocalFileSystem.apply(this);
        mRootFileSystem = newRootFileSystem.apply(this);
    }

    @NonNull
    protected <FS extends FileSystem> FS getLocalFileSystem() {
        //noinspection unchecked
        return (FS) mLocalFileSystem;
    }

    @NonNull
    protected <FS extends RootFileSystem> FS getRootFileSystem() {
        //noinspection unchecked
        return (FS) mRootFileSystem;
    }

    public void ensureRootInterface() throws RemoteFileSystemException {
        mRootFileSystem.ensureRemoteInterface();
    }

    @NonNull
    @Override
    public FileSystemProvider provider() {
        return mLocalFileSystem.provider();
    }

    @Override
    public void close() throws IOException {
        boolean wasOpen = mLocalFileSystem.isOpen();
        mLocalFileSystem.close();
        if (wasOpen) {
            mRootFileSystem.close();
        }
    }

    @Override
    public boolean isOpen() {
        return mLocalFileSystem.isOpen();
    }

    @Override
    public boolean isReadOnly() {
        return mLocalFileSystem.isReadOnly();
    }

    @NonNull
    @Override
    public String getSeparator() {
        return mLocalFileSystem.getSeparator();
    }

    @NonNull
    @Override
    public Iterable<Path> getRootDirectories() {
        return mLocalFileSystem.getRootDirectories();
    }

    @NonNull
    @Override
    public Iterable<FileStore> getFileStores() {
        // TODO: Consider using root? But when?
        return mLocalFileSystem.getFileStores();
    }

    @NonNull
    @Override
    public Set<String> supportedFileAttributeViews() {
        return mLocalFileSystem.supportedFileAttributeViews();
    }

    @NonNull
    @Override
    public Path getPath(@NonNull String first, @NonNull String... more) {
        return mLocalFileSystem.getPath(first, more);
    }

    @NonNull
    @Override
    public PathMatcher getPathMatcher(@NonNull String syntaxAndPattern) {
        return mLocalFileSystem.getPathMatcher(syntaxAndPattern);
    }

    @NonNull
    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return mLocalFileSystem.getUserPrincipalLookupService();
    }

    @NonNull
    @Override
    public WatchService newWatchService() throws IOException {
        // We don't have RemoteWatchService for now, and I doubt we can have one.
        return mLocalFileSystem.newWatchService();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        RootableFileSystem that = (RootableFileSystem) object;
        return Objects.equals(mLocalFileSystem, that.mLocalFileSystem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mLocalFileSystem);
    }
}
