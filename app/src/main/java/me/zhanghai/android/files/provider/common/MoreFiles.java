/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.CopyOption;
import java8.nio.file.Files;
import java8.nio.file.Path;
import java8.nio.file.ProviderMismatchException;
import java8.nio.file.spi.FileSystemProvider;
import me.zhanghai.android.files.util.IoUtils;

public class MoreFiles {

    private MoreFiles() {}

    // Can handle ProgressCopyOption.
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

    // Can handle ProgressCopyOption.
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

    // TODO: Just use Files.readAllBytes(), if all our providers support
    //  newByteChannel()?
    // Uses newInputStream() instead of newByteChannel().
    @NonNull
    public static byte[] readAllBytes(@NonNull Path path) throws IOException {
        Objects.requireNonNull(path);
        long sizeLong = Files.size(path);
        if (sizeLong > Integer.MAX_VALUE) {
            throw new OutOfMemoryError("size " + sizeLong);
        }
        int size = (int) sizeLong;
        try (InputStream inputStream = Files.newInputStream(path)) {
            return IoUtils.inputStreamToByteArray(inputStream, size);
        }
    }

    // Can resolve path in a foreign provider.
    @NonNull
    public static Path resolve(@NonNull Path path, @NonNull Path other) {
        ByteStringListPath byteStringPath = requireByteStringListPath(path);
        ByteStringListPath otherPath = requireByteStringListPath(other);
        if (otherPath.isAbsolute()) {
            return otherPath;
        }
        if (otherPath.isEmpty()) {
            return byteStringPath;
        }
        ByteStringListPath result = byteStringPath;
        for (int i = 0, count = otherPath.getNameCount(); i < count; ++i) {
            ByteString name = otherPath.getName(i).toByteString();
            result = result.resolve(name);
        }
        return result;
    }

    @NonNull
    private static ByteStringListPath requireByteStringListPath(@NonNull Path path) {
        if (!(path instanceof ByteStringListPath)) {
            throw new ProviderMismatchException(path.toString());
        }
        return (ByteStringListPath) path;
    }
}
