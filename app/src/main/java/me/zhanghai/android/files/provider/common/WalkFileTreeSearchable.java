/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.FileVisitResult;
import java8.nio.file.FileVisitor;
import java8.nio.file.Files;
import java8.nio.file.Path;
import java8.nio.file.attribute.BasicFileAttributes;
import java9.util.function.Consumer;
import me.zhanghai.android.files.util.MoreTextUtils;

public class WalkFileTreeSearchable {

    private WalkFileTreeSearchable() {}

    public static void search(@NonNull Path directory, @NonNull String query,
                              @NonNull Consumer<List<Path>> listener, long intervalMillis)
            throws IOException {
        List<Path> paths = new ArrayList<>();
        // Cannot use Files.find() or Files.walk() because it cannot ignore exceptions.
        Files.walkFileTree(directory, new FileVisitor<Path>() {
            private long mLastProgressMillis = System.currentTimeMillis();
            @NonNull
            @Override
            public FileVisitResult preVisitDirectory(@NonNull Path directory,
                                                     @NonNull BasicFileAttributes attributes)
                    throws InterruptedIOException {
                visit(directory);
                throwIfInterrupted();
                return FileVisitResult.CONTINUE;
            }
            @NonNull
            @Override
            public FileVisitResult visitFile(@NonNull Path file,
                                             @NonNull BasicFileAttributes attributes)
                    throws InterruptedIOException {
                visit(file);
                throwIfInterrupted();
                return FileVisitResult.CONTINUE;
            }
            @NonNull
            @Override
            public FileVisitResult visitFileFailed(@NonNull Path file,
                                                   @NonNull IOException exception)
                    throws InterruptedIOException {
                if (exception instanceof InterruptedIOException) {
                    throw (InterruptedIOException) exception;
                }
                exception.printStackTrace();
                visit(file);
                throwIfInterrupted();
                return FileVisitResult.CONTINUE;
            }
            @NonNull
            @Override
            public FileVisitResult postVisitDirectory(@NonNull Path directory,
                                                      @Nullable IOException exception)
                    throws InterruptedIOException {
                if (exception instanceof InterruptedIOException) {
                    throw (InterruptedIOException) exception;
                } else if (exception != null) {
                    exception.printStackTrace();
                }
                throwIfInterrupted();
                return FileVisitResult.CONTINUE;
            }
            private void visit(@NonNull Path path) {
                // Exclude the directory being searched.
                if (Objects.equals(path, directory)) {
                    return;
                }
                Path fileName = path.getFileName();
                if (fileName == null
                        || !MoreTextUtils.containsIgnoreCase(fileName.toString(), query)) {
                    return;
                }
                paths.add(path);
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis >= mLastProgressMillis + intervalMillis) {
                    listener.accept(paths);
                    mLastProgressMillis = currentTimeMillis;
                    paths.clear();
                }
            }
        });
        if (!paths.isEmpty()) {
            listener.accept(paths);
        }
    }

    private static void throwIfInterrupted() throws InterruptedIOException {
        if (Thread.interrupted()) {
            throw new InterruptedIOException();
        }
    }
}
