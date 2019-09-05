/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import java8.nio.file.attribute.FileAttributeView;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.provider.common.AbstractFileStore;
import me.zhanghai.android.files.provider.common.MoreFiles;

class LocalArchiveFileStore extends AbstractFileStore {

    @NonNull
    private final Path mArchiveFile;

    LocalArchiveFileStore(@NonNull Path archiveFile) {
        mArchiveFile = archiveFile;
    }

    @NonNull
    @Override
    public String name() {
        return mArchiveFile.toString();
    }

    @NonNull
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
        return MoreFiles.size(mArchiveFile);
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
        throw new AssertionError();
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull String name) {
        throw new AssertionError();
    }

    static boolean supportsFileAttributeView_(@NonNull Class<? extends FileAttributeView> type) {
        Objects.requireNonNull(type);
        return ArchiveFileSystemProvider.supportsFileAttributeView(type);
    }

    static boolean supportsFileAttributeView_(@NonNull String name) {
        Objects.requireNonNull(name);
        return ArchiveFileAttributeView.SUPPORTED_NAMES.contains(name);
    }
}
