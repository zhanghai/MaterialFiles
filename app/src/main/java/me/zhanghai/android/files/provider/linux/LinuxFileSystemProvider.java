/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.linux;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import java8.nio.file.attribute.FileAttributeView;
import java8.nio.file.spi.FileSystemProvider;
import me.zhanghai.android.files.provider.root.RootFileSystemProvider;
import me.zhanghai.android.files.provider.root.RootableFileSystemProvider;

public class LinuxFileSystemProvider extends RootableFileSystemProvider {

    static final String SCHEME = LocalLinuxFileSystemProvider.SCHEME;

    private static LinuxFileSystemProvider sInstance;
    private static final Object sInstanceLock = new Object();

    private LinuxFileSystemProvider() {
        super(provider -> new LocalLinuxFileSystemProvider((LinuxFileSystemProvider) provider),
                provider -> new RootFileSystemProvider(SCHEME));
    }

    public static void installAsDefault() {
        synchronized (sInstanceLock) {
            if (sInstance != null) {
                throw new IllegalStateException();
            }
            sInstance = new LinuxFileSystemProvider();
            FileSystemProvider.installDefaultProvider(sInstance);
        }
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    protected LocalLinuxFileSystemProvider getLocalProvider() {
        return super.getLocalProvider();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    protected RootFileSystemProvider getRootProvider() {
        return super.getRootProvider();
    }

    public static boolean isLinuxPath(@NonNull Path path) {
        return LocalLinuxFileSystemProvider.isLinuxPath(path);
    }

    @NonNull
    static LinuxFileSystem getFileSystem() {
        return sInstance.getLocalProvider().getFileSystem();
    }

    static boolean supportsFileAttributeView(@NonNull Class<? extends FileAttributeView> type) {
        return LocalLinuxFileSystemProvider.supportsFileAttributeView(type);
    }
}
