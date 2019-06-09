/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.CopyOption;
import java8.nio.file.Files;
import java8.nio.file.LinkOption;
import java8.nio.file.Path;
import java8.nio.file.ProviderMismatchException;
import java8.nio.file.attribute.BasicFileAttributes;
import java8.nio.file.spi.FileSystemProvider;
import java9.util.function.LongConsumer;
import me.zhanghai.android.files.util.IoUtils;

public class MoreFiles {

    private static final int BUFFER_SIZE = 8192;

    private MoreFiles() {}

    // Can handle ProgressCopyOption.
    public static void copy(@NonNull Path source, @NonNull Path target,
                            @NonNull CopyOption... options) throws IOException {
        FileSystemProvider sourceProvider = provider(source);
        if (sourceProvider == provider(target)) {
            sourceProvider.copy(source, target, options);
        } else {
            ForeignCopyMove.copy(source, target, options);
        }
    }

    public static void copy(@NonNull InputStream inputStream, @NonNull OutputStream outputStream,
                            @Nullable LongConsumer listener, int intervalMillis)
            throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long lastProgressMillis = System.currentTimeMillis();
        long copiedSize = 0;
        while (true) {
            int readSize = inputStream.read(buffer);
            if (readSize == -1) {
                break;
            }
            outputStream.write(buffer, 0, readSize);
            copiedSize += readSize;
            throwIfInterrupted();
            long currentTimeMillis = System.currentTimeMillis();
            if (listener != null && currentTimeMillis >= lastProgressMillis + intervalMillis) {
                listener.accept(copiedSize);
                lastProgressMillis = currentTimeMillis;
                copiedSize = 0;
            }
        }
        if (listener != null) {
            listener.accept(copiedSize);
        }
    }

    private static void throwIfInterrupted() throws InterruptedIOException {
        if (Thread.interrupted()) {
            throw new InterruptedIOException();
        }
    }

    // Can handle ProgressCopyOption.
    public static void move(@NonNull Path source, @NonNull Path target,
                            @NonNull CopyOption... options) throws IOException {
        FileSystemProvider sourceProvider = provider(source);
        if (sourceProvider == provider(target)) {
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
        long sizeLong = size(path);
        if (sizeLong > Integer.MAX_VALUE) {
            throw new OutOfMemoryError("size " + sizeLong);
        }
        int size = (int) sizeLong;
        try (InputStream inputStream = Files.newInputStream(path)) {
            return IoUtils.inputStreamToByteArray(inputStream, size);
        }
    }

    @NonNull
    public static ByteString readSymbolicLink(@NonNull Path link) throws IOException {
        Path target = Files.readSymbolicLink(link);
        if (!(target instanceof ByteStringPath)) {
            throw new ProviderMismatchException(target.toString());
        }
        ByteStringPath targetPath = (ByteStringPath) target;
        return targetPath.toByteString();
    }

    // Can resolve path in a foreign provider.
    @NonNull
    public static Path resolve(@NonNull Path path, @NonNull Path other) {
        ByteStringListPath byteStringPath = requireByteStringListPath(path);
        ByteStringListPath otherPath = requireByteStringListPath(other);
        if (provider(byteStringPath) == provider(otherPath)) {
            return byteStringPath.resolve(otherPath);
        }
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

    // Can accept link options.
    public static long size(@NonNull Path path, @NonNull LinkOption... options) throws IOException {
        return Files.readAttributes(path, BasicFileAttributes.class, options).size();
    }

    public static ByteString toByteString(@NonNull Path path) {
        ByteStringListPath byteStringPath = requireByteStringListPath(path);
        return byteStringPath.toByteString();
    }

    @NonNull
    private static ByteStringListPath requireByteStringListPath(@NonNull Path path) {
        if (!(path instanceof ByteStringListPath)) {
            throw new ProviderMismatchException(path.toString());
        }
        return (ByteStringListPath) path;
    }
}
