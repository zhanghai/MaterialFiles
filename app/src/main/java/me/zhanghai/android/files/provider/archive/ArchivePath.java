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
import me.zhanghai.android.files.provider.common.StringListPath;

class ArchivePath extends StringListPath {

    @NonNull
    private final ArchiveFileSystem mFileSystem;

    public ArchivePath(@NonNull ArchiveFileSystem fileSystem, @NonNull String path) {
        super(ArchiveFileSystem.SEPARATOR, path);

        mFileSystem = fileSystem;
    }

    private ArchivePath(@NonNull ArchiveFileSystem fileSystem, boolean absolute,
                        @NonNull List<String> names) {
        super(ArchiveFileSystem.SEPARATOR, absolute, names);

        mFileSystem = fileSystem;
    }

    @Override
    protected boolean isPathAbsolute(@NonNull String path) {
        Objects.requireNonNull(path);
        return !path.isEmpty() && path.charAt(0) == ArchiveFileSystem.SEPARATOR;
    }

    @NonNull
    @Override
    protected Path createPath(boolean absolute, @NonNull List<String> names) {
        Objects.requireNonNull(names);
        return new ArchivePath(mFileSystem, absolute, names);
    }

    @Nullable
    @Override
    protected String getUriSchemeSpecificPart() {
        return mFileSystem.getArchiveFile().toUri().toString();
    }

    @Nullable
    @Override
    protected String getUriFragment() {
        return super.getUriSchemeSpecificPart();
    }

    @NonNull
    @Override
    protected Path getDefaultDirectory() {
        return mFileSystem.getDefaultDirectory();
    }

    @NonNull
    @Override
    public FileSystem getFileSystem() {
        return mFileSystem;
    }

    @Nullable
    @Override
    public Path getRoot() {
        if (!isAbsolute()) {
            return null;
        }
        return mFileSystem.getRootDirectory();
    }

    @NonNull
    @Override
    public Path toRealPath(@NonNull LinkOption... options) throws IOException {
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
                             @NonNull WatchEvent.Modifier... modifiers) {
        Objects.requireNonNull(watcher);
        Objects.requireNonNull(events);
        Objects.requireNonNull(modifiers);
        throw new UnsupportedOperationException();
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeParcelable(mFileSystem, flags);
    }
}
