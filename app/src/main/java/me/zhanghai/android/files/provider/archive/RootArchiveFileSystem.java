/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import androidx.annotation.NonNull;
import java8.nio.file.FileSystem;
import me.zhanghai.android.files.provider.remote.RemoteFileSystemException;
import me.zhanghai.android.files.provider.root.RootFileService;
import me.zhanghai.android.files.provider.root.RootFileSystem;

class RootArchiveFileSystem extends RootFileSystem {

    private final FileSystem mFileSystem;

    private boolean mNeedRefresh;
    @NonNull
    private final Object mNeedRefreshLock = new Object();

    RootArchiveFileSystem(@NonNull FileSystem fileSystem) {
        super(fileSystem);

        mFileSystem = fileSystem;
    }

    public void refresh() {
        synchronized (mNeedRefreshLock) {
            if (hasRemoteInterface()) {
                mNeedRefresh = true;
            }
        }
    }

    public void doRefreshIfNeeded() throws RemoteFileSystemException {
        synchronized (mNeedRefreshLock) {
            if (mNeedRefresh) {
                if (hasRemoteInterface()) {
                    RootFileService.getInstance().refreshArchiveFileSystem(mFileSystem);
                }
                mNeedRefresh = false;
            }
        }
    }
}
