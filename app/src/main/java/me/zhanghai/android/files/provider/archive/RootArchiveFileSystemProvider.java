/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.AccessMode;
import java8.nio.file.DirectoryStream;
import java8.nio.file.OpenOption;
import java8.nio.file.Path;
import java8.nio.file.ProviderMismatchException;
import me.zhanghai.android.files.provider.remote.RemoteFileSystemException;
import me.zhanghai.android.files.provider.root.RootFileSystemProvider;

class RootArchiveFileSystemProvider extends RootFileSystemProvider {

    RootArchiveFileSystemProvider(@NonNull String scheme) {
        super(scheme);
    }

    @NonNull
    @Override
    public InputStream newInputStream(@NonNull Path file, @NonNull OpenOption... options)
            throws IOException {
        prepareFileSystem(file);

        return super.newInputStream(file, options);
    }

    @NonNull
    @Override
    public DirectoryStream<Path> newDirectoryStream(
            @NonNull Path directory, @NonNull DirectoryStream.Filter<? super Path> filter)
            throws IOException {
        prepareFileSystem(directory);

        return super.newDirectoryStream(directory, filter);
    }

    @NonNull
    @Override
    public Path readSymbolicLink(@NonNull Path link) throws IOException {
        prepareFileSystem(link);

        return super.readSymbolicLink(link);
    }

    @Override
    public void checkAccess(@NonNull Path path, @NonNull AccessMode... modes) throws IOException {
        prepareFileSystem(path);

        super.checkAccess(path, modes);
    }

    private void prepareFileSystem(@NonNull Path path) throws RemoteFileSystemException {
        requireArchivePath(path);
        ArchiveFileSystem fileSystem = (ArchiveFileSystem) path.getFileSystem();
        fileSystem.ensureRootInterface();
        fileSystem.doRefreshIfNeededAsRoot();
    }

    void doRefreshFileSystemIfNeeded(@NonNull Path path) throws RemoteFileSystemException {
        requireArchivePath(path);
        ArchiveFileSystem fileSystem = (ArchiveFileSystem) path.getFileSystem();
        fileSystem.doRefreshIfNeededAsRoot();
    }

    private static void requireArchivePath(@NonNull Path path) {
        Objects.requireNonNull(path);
        if (!(path instanceof ArchivePath)) {
            throw new ProviderMismatchException(path.toString());
        }
    }
}
