/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.CopyOption;
import java8.nio.file.Path;
import java8.nio.file.spi.FileSystemProvider;

public class MoreFiles {

    private MoreFiles() {}

    public static void copy(@NonNull Path source, @NonNull Path target,
                            @NonNull CopyOption... options) throws IOException {
        FileSystemProvider sourceProvider = provider(source);
        FileSystemProvider targetProvider = provider(target);
        if (sourceProvider == targetProvider) {
            sourceProvider.copy(source, target, options);
        } else {
            ForeignCopyMove.copy(source, target, options);
        }
    }

    public static void move(@NonNull Path source, @NonNull Path target,
                            @NonNull CopyOption... options) throws IOException {
        FileSystemProvider sourceProvider = provider(source);
        FileSystemProvider targetProvider = provider(target);
        if (sourceProvider == targetProvider) {
            sourceProvider.move(source, target, options);
        } else {
            ForeignCopyMove.move(source, target, options);
        }
    }

    @NonNull
    public static FileSystemProvider provider(@NonNull Path path) {
        Objects.requireNonNull(path);
        return path.getFileSystem().provider();
    }
}
