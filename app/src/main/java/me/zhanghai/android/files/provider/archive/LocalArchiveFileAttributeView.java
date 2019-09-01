/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java8.nio.file.Path;
import java8.nio.file.attribute.FileTime;
import me.zhanghai.android.files.provider.archive.archiver_sevenzipjbinding.ArchiveItem;
import me.zhanghai.android.files.provider.common.ByteString;
import me.zhanghai.android.files.provider.common.PosixFileAttributeView;
import me.zhanghai.android.files.provider.common.PosixFileModeBit;
import me.zhanghai.android.files.provider.common.PosixGroup;
import me.zhanghai.android.files.provider.common.PosixUser;

public class LocalArchiveFileAttributeView implements PosixFileAttributeView {

    private static final String NAME = ArchiveFileSystemProvider.SCHEME;

    static final Set<String> SUPPORTED_NAMES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("basic", "posix", NAME)));

    @NonNull
    private final Path mPath;

    LocalArchiveFileAttributeView(@NonNull Path path) {
        mPath = path;
    }

    @NonNull
    @Override
    public String name() {
        return NAME;
    }

    @NonNull
    @Override
    public ArchiveFileAttributes readAttributes() throws IOException {
        ArchiveFileSystem fileSystem = (ArchiveFileSystem) mPath.getFileSystem();
        ArchiveItem item = fileSystem.getItemAsLocal(mPath);
        return new ArchiveFileAttributes(fileSystem.getArchiveFile(), item);
    }

    @Override
    public void setTimes(@Nullable FileTime lastModifiedTime, @Nullable FileTime lastAccessTime,
                         @Nullable FileTime createTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOwner(@NonNull PosixUser owner) {
        Objects.requireNonNull(owner);
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGroup(@NonNull PosixGroup group) {
        Objects.requireNonNull(group);
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMode(@NonNull Set<PosixFileModeBit> mode) {
        Objects.requireNonNull(mode);
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSeLinuxContext(@NonNull ByteString context) {
        Objects.requireNonNull(context);
        throw new UnsupportedOperationException();
    }

    @Override
    public void restoreSeLinuxContext() {
        throw new UnsupportedOperationException();
    }
}
