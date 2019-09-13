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
import me.zhanghai.android.files.provider.content.ContentFileSystemProvider;
import me.zhanghai.android.files.provider.document.DocumentFileSystemProvider;
import me.zhanghai.android.files.provider.linux.LinuxFileSystemProvider;

public class FileSystemProviders {

    private static volatile boolean sOverflowWatchEvents;

    private FileSystemProviders() {}

    public static void install() {
        LinuxFileSystemProvider.installAsDefault();
        ArchiveFileSystemProvider.install();
        ContentFileSystemProvider.install();
        DocumentFileSystemProvider.install();
        AndroidFileTypeDetector.install();
    }

    public static boolean shouldOverflowWatchEvents() {
        return sOverflowWatchEvents;
    }

    /**
     * If set, WatchService implementations will skip processing any event data and simply send an
     * overflow event to all the registered keys upon successful read from the inotify fd. This can
     * help reducing the JNI and GC overhead when large amount of inotify events are generated.
     * Simply sending an overflow event to all the keys is okay because we use only one key per
     * service for WatchServicePathObservable.
     */
    public static void setOverflowWatchEvents(boolean overflowWatchEvents) {
        sOverflowWatchEvents = overflowWatchEvents;
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
