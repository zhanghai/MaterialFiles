/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.root;

import java.net.URI;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.FileSystem;
import java8.nio.file.LinkOption;
import java8.nio.file.Path;
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.attribute.FileAttributeView;
import me.zhanghai.android.files.provider.remote.RemoteFileSystemProvider;
import me.zhanghai.android.files.provider.remote.RemoteInterfaceHolder;

public class RootFileSystemProvider extends RemoteFileSystemProvider {

    public RootFileSystemProvider(@NonNull String scheme) {
        super(new RemoteInterfaceHolder<>(() -> RootFileService.getInstance()
                .getRemoteFileSystemProviderInterface(scheme)));
    }

    @NonNull
    @Override
    public String getScheme() {
        throw new AssertionError();
    }

    @NonNull
    @Override
    public FileSystem newFileSystem(@NonNull URI uri, @NonNull Map<String, ?> env) {
        throw new AssertionError();
    }

    @NonNull
    @Override
    public FileSystem getFileSystem(@NonNull URI uri) {
        throw new AssertionError();
    }

    @NonNull
    @Override
    public Path getPath(@NonNull URI uri) {
        throw new AssertionError();
    }

    @Nullable
    @Override
    public <V extends FileAttributeView> V getFileAttributeView(@NonNull Path path,
                                                                @NonNull Class<V> type,
                                                                @NonNull LinkOption... options) {
        throw new AssertionError();
    }

    @NonNull
    @Override
    public <A extends BasicFileAttributes> A readAttributes(@NonNull Path path,
                                                            @NonNull Class<A> type,
                                                            @NonNull LinkOption... options) {
        throw new AssertionError();
    }
}
