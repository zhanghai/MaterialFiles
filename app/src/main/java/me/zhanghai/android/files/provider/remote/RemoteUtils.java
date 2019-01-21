/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import java.net.URI;
import java.net.URISyntaxException;

import androidx.annotation.NonNull;
import java8.nio.file.FileSystem;
import java8.nio.file.Path;
import java8.nio.file.Paths;
import java8.nio.file.spi.FileSystemProvider;
import me.zhanghai.android.files.provider.common.StringListPath;
import me.zhanghai.android.files.provider.common.StringPath;

public class RemoteUtils {

    private static final String REMOTE_SCHEME_PREFIX = "remote-";

    private RemoteUtils() {}

    public static boolean isRemoteScheme(@NonNull String scheme) {
        return scheme.startsWith(REMOTE_SCHEME_PREFIX);
    }

    @NonNull
    public static String toRemoteScheme(@NonNull String scheme) {
        if (isRemoteScheme(scheme)) {
            throw new IllegalArgumentException("scheme is already a remote scheme: " + scheme);
        }
        return REMOTE_SCHEME_PREFIX + scheme;
    }

    @NonNull
    public static String toLocalScheme(@NonNull String scheme) {
        if (!isRemoteScheme(scheme)) {
            throw new IllegalArgumentException("scheme is not a remote scheme: " + scheme);
        }
        return scheme.substring(REMOTE_SCHEME_PREFIX.length());
    }

    public static boolean isRemoteUri(@NonNull URI uri) {
        return isRemoteScheme(uri.getScheme());
    }

    /**
     * @see StringListPath#toUri()
     */
    @NonNull
    public static URI toRemoteUri(@NonNull URI uri) {
        if (isRemoteUri(uri)) {
            throw new IllegalArgumentException("uri is already a remote URI: " + uri);
        }
        try {
            return new URI(toRemoteScheme(uri.getScheme()), uri.getPath(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * @see StringListPath#toUri()
     */
    @NonNull
    public static URI toLocalUri(@NonNull URI uri) {
        if (!isRemoteUri(uri)) {
            throw new IllegalArgumentException("uri is not a remote URI: " + uri);
        }
        try {
            return new URI(toLocalScheme(uri.getScheme()), uri.getPath(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    public static boolean isRemotePath(@NonNull Path path) {
        return isRemoteUri(path.toUri());
    }

    @NonNull
    public static Path toRemotePath(@NonNull Path path) {
        if (path instanceof StringPath) {
            return path;
        }
        if (isRemotePath(path)) {
            throw new IllegalArgumentException("path is already a remote path: " + path);
        }
        return Paths.get(toRemoteUri(path.toUri()));
    }

    @NonNull
    public static Path toLocalPath(@NonNull Path path) {
        if (path instanceof StringPath) {
            return path;
        }
        if (!isRemotePath(path)) {
            throw new IllegalArgumentException("path is not a remote path: " + path);
        }
        return Paths.get(toLocalUri(path.toUri()));
    }

    public static boolean isRemoteFileSystem(@NonNull FileSystem fileSystem) {
        return isRemotePath(fileSystem.getPath(""));
    }

    @NonNull
    public static FileSystem toRemoteFileSystem(@NonNull FileSystem fileSystem) {
        if (isRemoteFileSystem(fileSystem)) {
            throw new IllegalArgumentException("fileSystem is already a remote file system: "
                    + fileSystem);
        }
        return toRemotePath(fileSystem.getPath("")).getFileSystem();
    }

    @NonNull
    public static FileSystem toLocalFileSystem(@NonNull FileSystem fileSystem) {
        if (!isRemoteFileSystem(fileSystem)) {
            throw new IllegalArgumentException("fileSystem is not a remote file system: "
                    + fileSystem);
        }
        return toLocalPath(fileSystem.getPath("")).getFileSystem();
    }

    @NonNull
    public static FileSystemProvider getRemoteProvider(@NonNull String scheme) {
        if (isRemoteScheme(scheme)) {
            throw new IllegalArgumentException("scheme is already a remote scheme: " + scheme);
        }
        return findInstalledProvider(toRemoteScheme(scheme));
    }

    @NonNull
    public static FileSystemProvider getLocalProvider(@NonNull String scheme) {
        if (!isRemoteScheme(scheme)) {
            throw new IllegalArgumentException("scheme is not a remote scheme: " + scheme);
        }
        return findInstalledProvider(toLocalScheme(scheme));
    }

    @NonNull
    private static FileSystemProvider findInstalledProvider(@NonNull String scheme) {
        for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
            if (provider.getScheme().equalsIgnoreCase(scheme)) {
                return provider;
            }
        }
        throw new IllegalStateException("Cannot find provider with scheme: " + scheme);
    }
}
