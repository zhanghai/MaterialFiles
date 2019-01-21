/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.file.FileStore;
import java8.nio.file.Path;
import java8.nio.file.PathMatcher;
import java8.nio.file.attribute.UserPrincipalLookupService;
import me.zhanghai.android.files.provider.remote.RemoteFileSystem;

class RemoteLinuxFileSystem extends RemoteFileSystem implements Parcelable {

    static final char SEPARATOR = LinuxFileSystem.SEPARATOR;

    private static final String SEPARATOR_STRING = Character.toString(SEPARATOR);

    @NonNull
    private final RemoteLinuxPath mRootDirectory = new RemoteLinuxPath(this, "/");
    {
        if (!mRootDirectory.isAbsolute()) {
            throw new AssertionError("Root directory must be absolute");
        }
        if (mRootDirectory.getNameCount() != 0) {
            throw new AssertionError("Root directory must contain no names");
        }
    }

    @NonNull
    private final RemoteLinuxPath mDefaultDirectory;
    {
        String userDir = System.getenv("user.dir");
        if (userDir == null) {
            userDir = "/";
        }
        mDefaultDirectory = new RemoteLinuxPath(this, userDir);
        if (!mDefaultDirectory.isAbsolute()) {
            throw new AssertionError("Default directory must be absolute");
        }
    }

    RemoteLinuxFileSystem(@NonNull RemoteLinuxFileSystemProvider provider) {
        super(provider);
    }

    @NonNull
    Path getRootDirectory() {
        return mRootDirectory;
    }

    @NonNull
    Path getDefaultDirectory() {
        return mDefaultDirectory;
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @NonNull
    @Override
    public String getSeparator() {
        return SEPARATOR_STRING;
    }

    @NonNull
    @Override
    public Iterable<Path> getRootDirectories() {
        return Collections.singletonList(mRootDirectory);
    }

    @NonNull
    @Override
    public Iterable<FileStore> getFileStores() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public Set<String> supportedFileAttributeViews() {
        return RemoteLinuxFileAttributeView.SUPPORTED_NAMES;
    }

    @NonNull
    @Override
    public Path getPath(@NonNull String first, @NonNull String... more) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(more);
        StringBuilder pathBuilder = new StringBuilder(first);
        for (String name : more) {
            pathBuilder
                    .append(SEPARATOR)
                    .append(name);
        }
        String path = pathBuilder.toString();
        return new RemoteLinuxPath(this, path);
    }

    @NonNull
    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        Objects.requireNonNull(syntaxAndPattern);
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }


    public static final Creator<RemoteLinuxFileSystem> CREATOR =
            new Creator<RemoteLinuxFileSystem>() {
                @Override
                public RemoteLinuxFileSystem createFromParcel(Parcel source) {
                    return RemoteLinuxFileSystemProvider.getFileSystem();
                }
                @Override
                public RemoteLinuxFileSystem[] newArray(int size) {
                    return new RemoteLinuxFileSystem[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {}
}
