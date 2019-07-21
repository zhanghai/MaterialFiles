/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.content;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.util.Collections;
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
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.ByteStringListPathFactory;

class ContentFileSystem extends FileSystem implements ByteStringListPathFactory, Parcelable {

    @NonNull
    private final ContentFileSystemProvider mProvider;

    ContentFileSystem(@NonNull ContentFileSystemProvider provider) {
        mProvider = provider;
    }

    @NonNull
    @Override
    public FileSystemProvider provider() {
        return mProvider;
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
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public Iterable<Path> getRootDirectories() {
        return Collections.emptyList();
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
        return ContentFileAttributeView.SUPPORTED_NAMES;
    }

    @NonNull
    @Override
    public ContentPath getPath(@NonNull String first, @NonNull String... more) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(more);
        if (more.length != 0) {
            throw new UnsupportedOperationException();
        }
        Uri uri = Uri.parse(first);
        return new ContentPath(this, uri);
    }

    @NonNull
    @Override
    public ContentPath getPath(@NonNull ByteString first, @NonNull ByteString... more) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(more);
        if (more.length != 0) {
            throw new UnsupportedOperationException();
        }
        Uri uri = Uri.parse(first.toString());
        return new ContentPath(this, uri);
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

    @NonNull
    @Override
    public WatchService newWatchService() throws IOException {
        // TODO
        throw new UnsupportedOperationException();
    }


    public static final Creator<ContentFileSystem> CREATOR = new Creator<ContentFileSystem>() {
        @Override
        public ContentFileSystem createFromParcel(Parcel source) {
            return ContentFileSystemProvider.getFileSystem();
        }
        @Override
        public ContentFileSystem[] newArray(int size) {
            return new ContentFileSystem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {}
}
