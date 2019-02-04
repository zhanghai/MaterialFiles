/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider;

import androidx.annotation.NonNull;
import java8.nio.file.ProviderNotFoundException;
import java8.nio.file.spi.FileSystemProvider;
import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;
import me.zhanghai.android.files.provider.common.AndroidFileTypeDetector;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;
import me.zhanghai.android.files.provider.remote.AndroidRootFileService;
import me.zhanghai.android.files.provider.remote.RemoteFileService;

public class FileSystemProviders {

    private FileSystemProviders() {}

    public static void install() {
        LinuxFileSystemProvider.installAsDefault();
        ArchiveFileSystemProvider.install();
        RemoteFileService.use(new AndroidRootFileService());
        AndroidFileTypeDetector.install();
    }

    @NonNull
    public static FileSystemProvider get(@NonNull String scheme) {
        for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
            if (provider.getScheme().equalsIgnoreCase(scheme)) {
                return provider;
            }
        }
        throw new ProviderNotFoundException(scheme);
    }
}
