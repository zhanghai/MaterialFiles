/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import java.io.IOException;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import me.zhanghai.android.files.provider.common.PosixFileAttributeView;
import me.zhanghai.android.files.provider.common.PosixFileAttributes;
import me.zhanghai.android.files.provider.root.RootPosixFileAttributeView;

public class RootArchiveFileAttributeView extends RootPosixFileAttributeView {

    @NonNull
    private final Path mPath;

    public RootArchiveFileAttributeView(@NonNull PosixFileAttributeView attributeView,
                                        @NonNull Path path) {
        super(attributeView);

        mPath = path;
    }

    @NonNull
    @Override
    public PosixFileAttributes readAttributes() throws IOException {
        ArchiveFileSystemProvider.doRefreshFileSystemIfNeeded(mPath);
        return super.readAttributes();
    }
}
