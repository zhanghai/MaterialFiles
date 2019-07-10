/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import java8.nio.file.AtomicMoveNotSupportedException;
import java8.nio.file.CopyOption;
import java8.nio.file.FileAlreadyExistsException;
import java8.nio.file.Files;
import java8.nio.file.LinkOption;
import java8.nio.file.OpenOption;
import java8.nio.file.Path;
import java8.nio.file.StandardCopyOption;
import java8.nio.file.StandardOpenOption;
import java8.nio.file.attribute.BasicFileAttributeView;
import java8.nio.file.attribute.BasicFileAttributes;

class ForeignCopyMove {

    private ForeignCopyMove() {}

    public static void copy(@NonNull Path source, @NonNull Path target,
                            @NonNull CopyOption... options) throws IOException {
        CopyOptions copyOptions = CopyOptions.fromArray(options);
        if (copyOptions.hasAtomicMove()) {
            throw new UnsupportedOperationException(StandardCopyOption.ATOMIC_MOVE.toString());
        }
        LinkOption[] linkOptions = copyOptions.hasNoFollowLinks() ?
                new LinkOption[] { LinkOption.NOFOLLOW_LINKS } : new LinkOption[0];
        BasicFileAttributes sourceAttributes = Files.readAttributes(source,
                BasicFileAttributes.class, linkOptions);
        if (!(sourceAttributes.isRegularFile() || sourceAttributes.isDirectory()
                || sourceAttributes.isSymbolicLink())) {
            throw new IOException("Cannot copy special file to foreign provider");
        }
        if (!copyOptions.hasReplaceExisting() && Files.exists(target)) {
            throw new FileAlreadyExistsException(source.toString(), target.toString(), null);
        }
        if (sourceAttributes.isRegularFile()) {
            if (copyOptions.hasReplaceExisting()) {
                Files.deleteIfExists(target);
            }
            OpenOption[] openOptions = copyOptions.hasNoFollowLinks() ?
                    new OpenOption[] { LinkOption.NOFOLLOW_LINKS } : new OpenOption[0];
            try (InputStream inputStream = MoreFiles.newInputStream(source, openOptions)) {
                OutputStream outputStream = MoreFiles.newOutputStream(target,
                        StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                boolean successful = false;
                try {
                    MoreFiles.copy(inputStream, outputStream, copyOptions.getProgressListener(),
                            copyOptions.getProgressIntervalMillis());
                    successful = true;
                } finally {
                    try {
                        outputStream.close();
                    } finally {
                        if (!successful) {
                            try {
                                Files.deleteIfExists(target);
                            } catch (IOException | UnsupportedOperationException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } else if (sourceAttributes.isDirectory()) {
            if (copyOptions.hasReplaceExisting()) {
                Files.deleteIfExists(target);
            }
            Files.createDirectory(target);
            if (copyOptions.hasProgressListener()) {
                copyOptions.getProgressListener().accept(sourceAttributes.size());
            }
        } else if (sourceAttributes.isSymbolicLink()) {
            Path sourceTarget = Files.readSymbolicLink(source);
            try {
                // Might throw UnsupportedOperationException, so we cannot delete beforehand.
                Files.createSymbolicLink(target, sourceTarget);
            } catch (FileAlreadyExistsException e) {
                if (!copyOptions.hasReplaceExisting()) {
                    throw e;
                }
                Files.deleteIfExists(target);
                Files.createSymbolicLink(target, sourceTarget);
            }
            if (copyOptions.hasProgressListener()) {
                copyOptions.getProgressListener().accept(sourceAttributes.size());
            }
        } else {
            throw new AssertionError();
        }
        if (copyOptions.hasCopyAttributes()) {
            // We don't take error when copying attribute fatal, so errors will only be logged from
            // now on.
            BasicFileAttributeView targetAttributeView = Files.getFileAttributeView(target,
                    BasicFileAttributeView.class);
            try {
                targetAttributeView.setTimes(sourceAttributes.lastModifiedTime(),
                        sourceAttributes.lastAccessTime(), sourceAttributes.creationTime());
            } catch (IOException | UnsupportedOperationException e) {
                e.printStackTrace();
            }
        }
    }

    public static void move(@NonNull Path source, @NonNull Path target,
                            @NonNull CopyOption... options) throws IOException {
        CopyOptions copyOptions = CopyOptions.fromArray(options);
        if (copyOptions.hasAtomicMove()) {
            throw new AtomicMoveNotSupportedException(source.toString(), target.toString(),
                    "Cannot move file atomically to foreign provider");
        }
        CopyOption[] optionsForCopy;
        if (copyOptions.hasCopyAttributes() && copyOptions.hasNoFollowLinks()) {
            optionsForCopy = options;
        } else {
            optionsForCopy = new CopyOptions(copyOptions.hasReplaceExisting(), true, false, true,
                    copyOptions.getProgressListener(), copyOptions.getProgressIntervalMillis())
                    .toArray();
        }
        copy(source, target, optionsForCopy);
        try {
            Files.delete(source);
        } catch (IOException | UnsupportedOperationException e) {
            try {
                Files.delete(target);
            } catch (IOException | UnsupportedOperationException e2) {
                e.addSuppressed(e2);
            }
            throw e;
        }
    }
}
