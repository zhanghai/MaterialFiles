/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

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

class RemoteArchiveFileSystem extends RemoteFileSystem implements Parcelable {

    static final char SEPARATOR = ArchiveFileSystem.SEPARATOR;

    private static final String SEPARATOR_STRING = Character.toString(SEPARATOR);

    @NonNull
    private final RemoteArchivePath mRootDirectory = new RemoteArchivePath(this, "/");
    {
        if (!mRootDirectory.isAbsolute()) {
            throw new AssertionError("Root directory must be absolute");
        }
        if (mRootDirectory.getNameCount() != 0) {
            throw new AssertionError("Root directory must contain no names");
        }
    }

    @NonNull
    private final Path mArchiveFile;

    RemoteArchiveFileSystem(@NonNull RemoteArchiveFileSystemProvider provider,
                            @NonNull Path archiveFile) {
        super(provider);

        mArchiveFile = archiveFile;
    }

    @NonNull
    Path getRootDirectory() {
        return mRootDirectory;
    }

    @NonNull
    Path getDefaultDirectory() {
        return mRootDirectory;
    }

    @NonNull
    Path getArchiveFile() {
        return mArchiveFile;
    }

    @Override
    public boolean isReadOnly() {
        return true;
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
        return RemoteArchiveFileAttributeView.SUPPORTED_NAMES;
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
        return new RemoteArchivePath(this, path);
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


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        RemoteArchiveFileSystem that = (RemoteArchiveFileSystem) object;
        return Objects.equals(mArchiveFile, that.mArchiveFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mArchiveFile);
    }


    public static final Creator<RemoteArchiveFileSystem> CREATOR =
            new Creator<RemoteArchiveFileSystem>() {
                @Override
                public RemoteArchiveFileSystem createFromParcel(Parcel source) {
                    Path archiveFile = source.readParcelable(Path.class.getClassLoader());
                    return RemoteArchiveFileSystemProvider.getOrNewFileSystem(archiveFile);
                }
                @Override
                public RemoteArchiveFileSystem[] newArray(int size) {
                    return new RemoteArchiveFileSystem[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) mArchiveFile, flags);
    }
}
