/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.FileStore;
import java8.nio.file.Files;
import java8.nio.file.Path;
import java8.nio.file.attribute.FileAttributeView;
import java8.nio.file.attribute.FileStoreAttributeView;
import me.zhanghai.android.files.file.MimeTypes;

public class ArchiveFileStore extends FileStore {

    @NonNull
    private final Path mArchiveFile;

    ArchiveFileStore(@NonNull Path archiveFile) {
        mArchiveFile = archiveFile;
    }

    @Override
    public String name() {
        return mArchiveFile.toString();
    }

    @Override
    public String type() {
        return MimeTypes.getMimeType(mArchiveFile.toString());
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public long getTotalSpace() throws IOException {
        return Files.size(mArchiveFile);
    }

    @Override
    public long getUsableSpace() {
        return 0;
    }

    @Override
    public long getUnallocatedSpace() {
        return 0;
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull Class<? extends FileAttributeView> type) {
        Objects.requireNonNull(type);
        return ArchiveFileSystemProvider.supportsFileAttributeView(type);
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull String name) {
        Objects.requireNonNull(name);
        return ArchiveFileAttributeView.SUPPORTED_NAMES.contains(name);
    }

    @Nullable
    @Override
    public <V extends FileStoreAttributeView> V getFileStoreAttributeView(@NonNull Class<V> type) {
        Objects.requireNonNull(type);
        return null;
    }

    @Nullable
    @Override
    public Object getAttribute(@NonNull String attribute) {
        Objects.requireNonNull(attribute);
        throw new UnsupportedOperationException();
    }
}
