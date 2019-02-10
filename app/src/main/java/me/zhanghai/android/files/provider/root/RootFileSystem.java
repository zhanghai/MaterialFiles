/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root;

import java.io.IOException;
import java.util.Set;

import androidx.annotation.NonNull;
import java8.nio.file.FileStore;
import java8.nio.file.FileSystem;
import java8.nio.file.Path;
import java8.nio.file.PathMatcher;
import java8.nio.file.WatchService;
import java8.nio.file.attribute.UserPrincipalLookupService;
import java8.nio.file.spi.FileSystemProvider;
import me.zhanghai.android.files.provider.remote.RemoteFileSystem;
import me.zhanghai.android.files.provider.remote.RemoteInterfaceHolder;

public class RootFileSystem extends RemoteFileSystem {

    public RootFileSystem(@NonNull FileSystem fileSystem) {
        super(new RemoteInterfaceHolder<>(() -> RootFileService.getInstance()
                .getRemoteFileSystemInterface(fileSystem)));
    }

    @NonNull
    @Override
    public FileSystemProvider provider() {
        throw new AssertionError();
    }

    @Override
    public boolean isOpen() {
        throw new AssertionError();
    }

    @Override
    public boolean isReadOnly() {
        throw new AssertionError();
    }

    @NonNull
    @Override
    public String getSeparator() {
        throw new AssertionError();
    }

    @NonNull
    @Override
    public Iterable<Path> getRootDirectories() {
        throw new AssertionError();
    }

    @NonNull
    @Override
    public Iterable<FileStore> getFileStores() {
        throw new AssertionError();
    }

    @NonNull
    @Override
    public Set<String> supportedFileAttributeViews() {
        throw new AssertionError();
    }

    @NonNull
    @Override
    public Path getPath(@NonNull String first, @NonNull String... more) {
        throw new AssertionError();
    }

    @NonNull
    @Override
    public PathMatcher getPathMatcher(@NonNull String syntaxAndPattern) {
        throw new AssertionError();
    }

    @NonNull
    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new AssertionError();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException();
    }
}
