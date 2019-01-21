/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.FileSystem;
import java8.nio.file.FileSystemAlreadyExistsException;
import java8.nio.file.LinkOption;
import java8.nio.file.Path;
import java8.nio.file.ProviderMismatchException;
import java8.nio.file.attribute.FileAttributeView;
import java8.nio.file.spi.FileSystemProvider;
import me.zhanghai.android.files.provider.common.LinkOptions;
import me.zhanghai.android.files.provider.remote.RemoteFileSystemProvider;
import me.zhanghai.android.files.provider.remote.RemoteUtils;

public class RemoteLinuxFileSystemProvider extends RemoteFileSystemProvider {

    static final String SCHEME = RemoteUtils.toRemoteScheme(LinuxFileSystemProvider.SCHEME);

    private static RemoteLinuxFileSystemProvider sInstance;
    private static final Object sInstanceLock = new Object();

    @NonNull
    private final RemoteLinuxFileSystem mFileSystem = new RemoteLinuxFileSystem(this);

    private RemoteLinuxFileSystemProvider() {}

    public static void install() {
        synchronized (sInstanceLock) {
            if (sInstance != null) {
                throw new IllegalStateException();
            }
            sInstance = new RemoteLinuxFileSystemProvider();
            FileSystemProvider.installProvider(sInstance);
        }
    }

    public static boolean isRemoteLinuxPath(@NonNull Path path) {
        Objects.requireNonNull(path);
        return path instanceof RemoteLinuxPath;
    }

    @NonNull
    static RemoteLinuxFileSystem getFileSystem() {
        return sInstance.mFileSystem;
    }

    @NonNull
    @Override
    public String getScheme() {
        return SCHEME;
    }

    @NonNull
    @Override
    public FileSystem newFileSystem(@NonNull URI uri, @NonNull Map<String, ?> env) {
        Objects.requireNonNull(uri);
        requireSameScheme(uri);
        Objects.requireNonNull(env);
        throw new FileSystemAlreadyExistsException();
    }

    @NonNull
    @Override
    public FileSystem getFileSystem(@NonNull URI uri) {
        Objects.requireNonNull(uri);
        requireSameScheme(uri);
        return mFileSystem;
    }

    @Override
    protected void removeFileSystem(@NonNull FileSystem fileSystem) {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public Path getPath(@NonNull URI uri) {
        Objects.requireNonNull(uri);
        requireSameScheme(uri);
        String path = uri.getPath();
        if (path == null) {
            throw new IllegalArgumentException("URI must have a path");
        }
        return mFileSystem.getPath(path);
    }

    private static void requireSameScheme(@NonNull URI uri) {
        if (!Objects.equals(uri.getScheme(), SCHEME)) {
            throw new IllegalArgumentException("URI scheme must be \"" + SCHEME + "\"");
        }
    }

    @Nullable
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(@NonNull Path path,
                                                                @NonNull Class<V> type,
                                                                @NonNull LinkOption... options) {
        requireRemoteLinuxPath(path);
        Objects.requireNonNull(type);
        Objects.requireNonNull(options);
        if (!supportsFileAttributeView(type)) {
            return null;
        }
        String pathString = path.toString();
        boolean noFollowLinks = LinkOptions.hasNoFollowLinks(options);
        //noinspection unchecked
        return (V) new RemoteLinuxFileAttributeView(pathString, noFollowLinks);
    }

    static boolean supportsFileAttributeView(@NonNull Class<? extends FileAttributeView> type) {
        return type.isAssignableFrom(RemoteLinuxFileAttributeView.class);
    }

    private static void requireRemoteLinuxPath(@NonNull Path path) {
        Objects.requireNonNull(path);
        if (!(path instanceof RemoteLinuxPath)) {
            throw new ProviderMismatchException(path.toString());
        }
    }
}
