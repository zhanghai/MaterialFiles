/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider;

import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;
import me.zhanghai.android.files.provider.archive.RemoteArchiveFileSystemProvider;
import me.zhanghai.android.files.provider.common.AndroidFileTypeDetector;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;
import me.zhanghai.android.files.provider.remote.RemoteFileService;
import me.zhanghai.android.files.provider.remote.AndroidRootFileService;

public class FileSystemProviders {

    private FileSystemProviders() {}

    public static void install() {
        LinuxFileSystemProvider.installAsDefault();
        ArchiveFileSystemProvider.install();
        AndroidFileTypeDetector.install();

        RemoteArchiveFileSystemProvider.install();
        RemoteFileService.use(new AndroidRootFileService());
    }
}
