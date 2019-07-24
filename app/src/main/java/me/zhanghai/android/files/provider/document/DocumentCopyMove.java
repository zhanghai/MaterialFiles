/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.document;

import android.net.Uri;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.FileAlreadyExistsException;
import java8.nio.file.StandardCopyOption;
import java9.util.function.LongConsumer;
import me.zhanghai.android.files.provider.common.CopyOptions;
import me.zhanghai.android.files.provider.content.resolver.ResolverException;
import me.zhanghai.android.files.provider.document.resolver.DocumentResolver;

class DocumentCopyMove {

    private DocumentCopyMove() {}

    @NonNull
    public static Uri copy(@NonNull DocumentPath source, @NonNull DocumentPath target,
                           @NonNull CopyOptions copyOptions) throws IOException {
        if (copyOptions.hasAtomicMove()) {
            throw new UnsupportedOperationException(StandardCopyOption.ATOMIC_MOVE.toString());
        }
        if (Objects.equals(source, target)) {
            Uri targetUri;
            try {
                targetUri = DocumentResolver.getDocumentUri(target);
            } catch (ResolverException e) {
                throw e.toFileSystemException(target.toString());
            }
            maybeNotifyListenerWithSize(targetUri, copyOptions);
            return targetUri;
        }
        boolean targetExists = DocumentResolver.exists(target);
        if (targetExists) {
            if (!copyOptions.hasReplaceExisting()) {
                throw new FileAlreadyExistsException(target.toString());
            }
            try {
                DocumentResolver.remove(target);
            } catch (ResolverException e) {
                throw e.toFileSystemException(target.toString());
            }
        }
        try {
            return DocumentResolver.copy(source, target, copyOptions.getProgressListener(),
                    copyOptions.getProgressIntervalMillis());
        } catch (ResolverException e) {
            throw e.toFileSystemException(source.toString(), target.toString());
        }
    }

    @NonNull
    public static Uri move(@NonNull DocumentPath source, @NonNull DocumentPath target,
                           @NonNull CopyOptions copyOptions) throws IOException {
        if (Objects.equals(source, target)) {
            Uri targetUri;
            try {
                targetUri = DocumentResolver.getDocumentUri(target);
            } catch (ResolverException e) {
                throw e.toFileSystemException(target.toString());
            }
            maybeNotifyListenerWithSize(targetUri, copyOptions);
            return targetUri;
        }
        boolean targetExists = DocumentResolver.exists(target);
        if (targetExists) {
            if (!copyOptions.hasReplaceExisting()) {
                throw new FileAlreadyExistsException(target.toString());
            }
            try {
                DocumentResolver.remove(target);
            } catch (ResolverException e) {
                throw e.toFileSystemException(target.toString());
            }
        }
        try {
            return DocumentResolver.move(source, target, copyOptions.hasAtomicMove(),
                    copyOptions.getProgressListener(), copyOptions.getProgressIntervalMillis());
        } catch (ResolverException e) {
            throw e.toFileSystemException(source.toString(), target.toString());
        }
    }

    private static void maybeNotifyListenerWithSize(@NonNull Uri uri,
                                                    @NonNull CopyOptions copyOptions) {
        LongConsumer listener = copyOptions.getProgressListener();
        if (listener == null) {
            return;
        }
        long size;
        try {
            size = DocumentResolver.getSize(uri);
        } catch (ResolverException e) {
            e.printStackTrace();
            return;
        }
        listener.accept(size);
    }
}
