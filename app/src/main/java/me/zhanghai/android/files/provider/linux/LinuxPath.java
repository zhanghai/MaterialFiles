/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
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
import me.zhanghai.android.files.provider.root.RootablePath;

class LinuxPath extends StringListPath implements RootablePath {

    @NonNull
    private final LinuxFileSystem mFileSystem;

    private volatile boolean mUseRoot;

    public LinuxPath(@NonNull LinuxFileSystem fileSystem, @NonNull String path) {
        super(LinuxFileSystem.SEPARATOR, path);

        mFileSystem = fileSystem;
    }

    private LinuxPath(@NonNull LinuxFileSystem fileSystem, boolean absolute,
                      @NonNull List<String> names) {
        super(LinuxFileSystem.SEPARATOR, absolute, names);

        mFileSystem = fileSystem;
    }

    @Override
    protected boolean isPathAbsolute(@NonNull String path) {
        Objects.requireNonNull(path);
        return !path.isEmpty() && path.charAt(0) == LinuxFileSystem.SEPARATOR;
    }

    @NonNull
    @Override
    protected Path createPath(boolean absolute, @NonNull List<String> names) {
        Objects.requireNonNull(names);
        return new LinuxPath(mFileSystem, absolute, names);
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
        return new File(toString());
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
    public boolean canUseRoot() {
        return true;
    }

    @Override
    public boolean shouldUseRoot() {
        return mUseRoot;
    }

    @Override
    public void setUseRoot() {
        mUseRoot = true;
    }


    public static final Creator<LinuxPath> CREATOR = new Creator<LinuxPath>() {
        @Override
        public LinuxPath createFromParcel(Parcel source) {
            return new LinuxPath(source);
        }
        @Override
        public LinuxPath[] newArray(int size) {
            return new LinuxPath[size];
        }
    };

    protected LinuxPath(Parcel in) {
        super(in);

        mFileSystem = in.readParcelable(LinuxFileSystem.class.getClassLoader());
        mUseRoot = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeParcelable(mFileSystem, flags);
        dest.writeByte(mUseRoot ? (byte) 1 : (byte) 0);
    }
}
