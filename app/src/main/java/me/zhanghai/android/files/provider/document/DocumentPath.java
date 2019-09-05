/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.document;

import android.net.Uri;
import android.os.Parcel;
import android.provider.DocumentsContract;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.FileSystem;
import java8.nio.file.LinkOption;
import java8.nio.file.WatchEvent;
import java8.nio.file.WatchKey;
import java8.nio.file.WatchService;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.ByteStringListPath;
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver;

/**
 * @see DocumentsContract.Path
 */
class DocumentPath extends ByteStringListPath<DocumentPath> implements DocumentResolver.Path {

    @NonNull
    private final DocumentFileSystem mFileSystem;

    DocumentPath(@NonNull DocumentFileSystem fileSystem, @NonNull ByteString path) {
        super(DocumentFileSystem.SEPARATOR, path);

        mFileSystem = fileSystem;
    }

    private DocumentPath(@NonNull DocumentFileSystem fileSystem, boolean absolute,
                        @NonNull List<ByteString> names) {
        super(DocumentFileSystem.SEPARATOR, absolute, names);

        mFileSystem = fileSystem;
    }

    @Override
    protected boolean isPathAbsolute(@NonNull ByteString path) {
        Objects.requireNonNull(path);
        return !path.isEmpty() && path.byteAt(0) == DocumentFileSystem.SEPARATOR;
    }

    @Override
    protected DocumentPath createPath(@NonNull ByteString path) {
        Objects.requireNonNull(path);
        return new DocumentPath(mFileSystem, path);
    }

    @NonNull
    @Override
    protected DocumentPath createPath(boolean absolute, @NonNull List<ByteString> names) {
        Objects.requireNonNull(names);
        return new DocumentPath(mFileSystem, absolute, names);
    }

    @Nullable
    @Override
    protected ByteString getUriSchemeSpecificPart() {
        return ByteString.fromString(mFileSystem.getTreeUri().toString());
    }

    @Nullable
    @Override
    protected ByteString getUriFragment() {
        return super.getUriSchemeSpecificPart();
    }

    @NonNull
    @Override
    protected DocumentPath getDefaultDirectory() {
        return mFileSystem.getDefaultDirectory();
    }

    @NonNull
    @Override
    public FileSystem getFileSystem() {
        return mFileSystem;
    }

    @Nullable
    @Override
    public DocumentPath getRoot() {
        if (!isAbsolute()) {
            return null;
        }
        return mFileSystem.getRootDirectory();
    }

    @NonNull
    @Override
    public DocumentPath toRealPath(@NonNull LinkOption... options) throws IOException {
        Objects.requireNonNull(options);
        // TODO
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public final File toFile() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public WatchKey register(@NonNull WatchService watcher, @NonNull WatchEvent.Kind<?>[] events,
                             @NonNull WatchEvent.Modifier... modifiers) throws IOException {
        Objects.requireNonNull(watcher);
        Objects.requireNonNull(events);
        Objects.requireNonNull(modifiers);
        // TODO
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public Uri getTreeUri() {
        return mFileSystem.getTreeUri();
    }

    @Nullable
    @Override
    public String getDisplayName() {
        ByteString name = getByteStringFileName();
        return name != null ? name.toString() : null;
    }


    public static final Creator<DocumentPath> CREATOR = new Creator<DocumentPath>() {
        @Override
        public DocumentPath createFromParcel(Parcel source) {
            return new DocumentPath(source);
        }
        @Override
        public DocumentPath[] newArray(int size) {
            return new DocumentPath[size];
        }
    };

    protected DocumentPath(Parcel in) {
        super(in);

        mFileSystem = in.readParcelable(DocumentFileSystem.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeParcelable(mFileSystem, flags);
    }
}
