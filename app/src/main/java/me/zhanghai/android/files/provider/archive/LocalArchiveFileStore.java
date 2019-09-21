/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import org.tukaani.xz.UnsupportedOptionsException;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import java8.nio.file.attribute.FileAttributeView;
import me.zhanghai.android.files.file.MimeTypes;
import me.zhanghai.android.files.provider.common.PosixFileStore;
import me.zhanghai.android.files.provider.common.MoreFiles;

class LocalArchiveFileStore extends PosixFileStore {

    @NonNull
    private final Path mArchiveFile;

    LocalArchiveFileStore(@NonNull Path archiveFile) {
        mArchiveFile = archiveFile;
    }

    @Override
    public void refresh() {}

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
    public void setReadOnly(boolean readOnly) throws IOException {
        throw new UnsupportedOptionsException();
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
        Objects.requireNonNull(type);
        return ArchiveFileSystemProvider.supportsFileAttributeView(type);
    }

    @Override
    public boolean supportsFileAttributeView(@NonNull String name) {
        Objects.requireNonNull(name);
        return ArchiveFileAttributeView.SUPPORTED_NAMES.contains(name);
    }
}
