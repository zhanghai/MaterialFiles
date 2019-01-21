/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

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

class RemoteLinuxPath extends StringListPath {

    @NonNull
    private final RemoteLinuxFileSystem mFileSystem;

    RemoteLinuxPath(@NonNull RemoteLinuxFileSystem fileSystem, @NonNull String path) {
        super(RemoteLinuxFileSystem.SEPARATOR, path);

        mFileSystem = fileSystem;
    }

    private RemoteLinuxPath(@NonNull RemoteLinuxFileSystem fileSystem, boolean absolute,
                            @NonNull List<String> names) {
        super(RemoteLinuxFileSystem.SEPARATOR, absolute, names);

        mFileSystem = fileSystem;
    }

    @Override
    protected boolean isPathAbsolute(@NonNull String path) {
        Objects.requireNonNull(path);
        return !path.isEmpty() && path.charAt(0) == RemoteLinuxFileSystem.SEPARATOR;
    }

    @NonNull
    @Override
    protected Path createPath(boolean absolute, @NonNull List<String> names) {
        Objects.requireNonNull(names);
        return new RemoteLinuxPath(mFileSystem, absolute, names);
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
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public final File toFile() {
        return new File(toString());
    }

    @NonNull
    @Override
    public WatchKey register(@NonNull WatchService watcher, @NonNull WatchEvent.Kind<?>[] events,
                             @NonNull WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException();
    }


    public static final Creator<RemoteLinuxPath> CREATOR = new Creator<RemoteLinuxPath>() {
        @Override
        public RemoteLinuxPath createFromParcel(Parcel source) {
            return new RemoteLinuxPath(source);
        }
        @Override
        public RemoteLinuxPath[] newArray(int size) {
            return new RemoteLinuxPath[size];
        }
    };

    protected RemoteLinuxPath(Parcel in) {
        super(in);

        mFileSystem = in.readParcelable(RemoteLinuxFileSystem.class.getClassLoader());
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
