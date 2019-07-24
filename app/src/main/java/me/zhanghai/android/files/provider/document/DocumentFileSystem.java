/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.document;

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
import me.zhanghai.android.files.provider.common.ByteStringBuilder;
import me.zhanghai.android.files.provider.common.ByteStringListPathFactory;

class DocumentFileSystem extends FileSystem implements ByteStringListPathFactory, Parcelable {

    static final byte SEPARATOR = '/';

    private static final ByteString SEPARATOR_BYTE_STRING = ByteString.ofByte(SEPARATOR);
    private static final String SEPARATOR_STRING = Character.toString((char) SEPARATOR);

    @NonNull
    private final DocumentFileSystemProvider mProvider;

    @NonNull
    private final Uri mTreeUri;

    @NonNull
    private final DocumentPath mRootDirectory;

    @NonNull
    private final Object mLock = new Object();

    private boolean mOpen = true;

    DocumentFileSystem(@NonNull DocumentFileSystemProvider provider, @NonNull Uri treeUri) {
        mProvider = provider;
        mTreeUri = treeUri;

        mRootDirectory = new DocumentPath(this, SEPARATOR_BYTE_STRING);
        if (!mRootDirectory.isAbsolute()) {
            throw new AssertionError("Root directory must be absolute");
        }
        if (mRootDirectory.getNameCount() != 0) {
            throw new AssertionError("Root directory must contain no names");
        }
    }

    @NonNull
    DocumentPath getRootDirectory() {
        return mRootDirectory;
    }

    @NonNull
    DocumentPath getDefaultDirectory() {
        return mRootDirectory;
    }

    @NonNull
    Uri getTreeUri() {
        return mTreeUri;
    }

    @NonNull
    @Override
    public FileSystemProvider provider() {
        return mProvider;
    }

    @Override
    public void close() {
        synchronized (mLock) {
            if (!mOpen) {
                return;
            }
            mProvider.removeFileSystem(this);
            mOpen = false;
        }
    }

    @Override
    public boolean isOpen() {
        synchronized (mLock) {
            return mOpen;
        }
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
        return DocumentFileAttributeView.SUPPORTED_NAMES;
    }

    @NonNull
    @Override
    public DocumentPath getPath(@NonNull String first, @NonNull String... more) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(more);
        ByteStringBuilder pathBuilder = new ByteStringBuilder(ByteString.fromString(first));
        for (String name : more) {
            Objects.requireNonNull(name);
            pathBuilder
                    .append(SEPARATOR)
                    .append(ByteString.fromString(name));
        }
        ByteString path = pathBuilder.toByteString();
        return new DocumentPath(this, path);
    }

    @NonNull
    @Override
    public DocumentPath getPath(@NonNull ByteString first, @NonNull ByteString... more) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(more);
        ByteStringBuilder pathBuilder = new ByteStringBuilder(first);
        for (ByteString name : more) {
            Objects.requireNonNull(name);
            pathBuilder
                    .append(SEPARATOR)
                    .append(name);
        }
        ByteString path = pathBuilder.toByteString();
        return new DocumentPath(this, path);
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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        DocumentFileSystem that = (DocumentFileSystem) object;
        return Objects.equals(mTreeUri, that.mTreeUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mTreeUri);
    }


    public static final Creator<DocumentFileSystem> CREATOR = new Creator<DocumentFileSystem>() {
        @Override
        public DocumentFileSystem createFromParcel(Parcel source) {
            Uri treeUri = source.readParcelable(Path.class.getClassLoader());
            return DocumentFileSystemProvider.getOrNewFileSystem(treeUri);
        }
        @Override
        public DocumentFileSystem[] newArray(int size) {
            return new DocumentFileSystem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mTreeUri, flags);
    }
}
