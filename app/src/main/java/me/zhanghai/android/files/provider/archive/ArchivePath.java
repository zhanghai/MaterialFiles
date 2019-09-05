/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import android.os.Parcel;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.FileSystem;
import java8.nio.file.LinkOption;
import java8.nio.file.Path;
import java8.nio.file.WatchEvent;
import java8.nio.file.WatchKey;
import java8.nio.file.WatchService;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.ByteStringListPath;
import me.zhanghai.android.files.provider.root.RootStrategy;
import me.zhanghai.android.files.provider.root.RootablePath;

class ArchivePath extends ByteStringListPath<ArchivePath> implements RootablePath {

    @NonNull
    private final ArchiveFileSystem mFileSystem;

    ArchivePath(@NonNull ArchiveFileSystem fileSystem, @NonNull ByteString path) {
        super(ArchiveFileSystem.SEPARATOR, path);

        mFileSystem = fileSystem;
    }

    private ArchivePath(@NonNull ArchiveFileSystem fileSystem, boolean absolute,
                        @NonNull List<ByteString> names) {
        super(ArchiveFileSystem.SEPARATOR, absolute, names);

        mFileSystem = fileSystem;
    }

    @Override
    protected boolean isPathAbsolute(@NonNull ByteString path) {
        Objects.requireNonNull(path);
        return !path.isEmpty() && path.byteAt(0) == ArchiveFileSystem.SEPARATOR;
    }

    @Override
    protected ArchivePath createPath(@NonNull ByteString path) {
        Objects.requireNonNull(path);
        return new ArchivePath(mFileSystem, path);
    }

    @NonNull
    @Override
    protected ArchivePath createPath(boolean absolute, @NonNull List<ByteString> names) {
        Objects.requireNonNull(names);
        return new ArchivePath(mFileSystem, absolute, names);
    }

    @Nullable
    @Override
    protected ByteString getUriSchemeSpecificPart() {
        return ByteString.fromString(mFileSystem.getArchiveFile().toUri().toString());
    }

    @Nullable
    @Override
    protected ByteString getUriFragment() {
        return super.getUriSchemeSpecificPart();
    }

    @NonNull
    @Override
    protected ArchivePath getDefaultDirectory() {
        return mFileSystem.getDefaultDirectory();
    }

    @NonNull
    @Override
    public FileSystem getFileSystem() {
        return mFileSystem;
    }

    @Nullable
    @Override
    public ArchivePath getRoot() {
        if (!isAbsolute()) {
            return null;
        }
        return mFileSystem.getRootDirectory();
    }

    @NonNull
    @Override
    public ArchivePath toRealPath(@NonNull LinkOption... options) throws IOException {
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

    @Override
    public boolean shouldPreferRoot() {
        Path archiveFile = mFileSystem.getArchiveFile();
        if (!(archiveFile instanceof RootablePath)) {
            return false;
        }
        RootablePath rootablePath = (RootablePath) archiveFile;
        return rootablePath.shouldPreferRoot();
    }

    @Override
    public void setPreferRoot() {
        Path archiveFile = mFileSystem.getArchiveFile();
        if (!(archiveFile instanceof RootablePath)) {
            throw new UnsupportedOperationException(archiveFile.toString());
        }
        RootablePath rootablePath = (RootablePath) archiveFile;
        rootablePath.setPreferRoot();
    }

    @NonNull
    @Override
    public RootStrategy getRootStrategy() {
        Path archiveFile = mFileSystem.getArchiveFile();
        if (!(archiveFile instanceof RootablePath)) {
            return RootStrategy.NEVER;
        }
        RootablePath rootablePath = (RootablePath) archiveFile;
        return rootablePath.getRootStrategy();
    }


    public static final Creator<ArchivePath> CREATOR = new Creator<ArchivePath>() {
        @Override
        public ArchivePath createFromParcel(Parcel source) {
            return new ArchivePath(source);
        }
        @Override
        public ArchivePath[] newArray(int size) {
            return new ArchivePath[size];
        }
    };

    protected ArchivePath(Parcel in) {
        super(in);

        mFileSystem = in.readParcelable(ArchiveFileSystem.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeParcelable(mFileSystem, flags);
    }
}
