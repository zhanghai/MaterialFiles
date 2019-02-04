/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.archive;

import androidx.annotation.NonNull;
import java8.nio.file.Path;
import java8.nio.file.attribute.FileAttributeView;
import java8.nio.file.spi.FileSystemProvider;
import me.zhanghai.android.files.provider.remote.RemoteFileSystemException;
import me.zhanghai.android.files.provider.root.RootableFileSystemProvider;

public class ArchiveFileSystemProvider extends RootableFileSystemProvider {

    static final String SCHEME = LocalArchiveFileSystemProvider.SCHEME;

    private static ArchiveFileSystemProvider sInstance;
    private static final Object sInstanceLock = new Object();

    private ArchiveFileSystemProvider() {
        super(provider -> new LocalArchiveFileSystemProvider((ArchiveFileSystemProvider) provider),
                provider -> new RootArchiveFileSystemProvider(SCHEME));
    }

    public static void install() {
        synchronized (sInstanceLock) {
            if (sInstance != null) {
                throw new IllegalStateException();
            }
            sInstance = new ArchiveFileSystemProvider();
            FileSystemProvider.installProvider(sInstance);
        }
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    protected LocalArchiveFileSystemProvider getLocalProvider() {
        return super.getLocalProvider();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    protected RootArchiveFileSystemProvider getRootProvider() {
        return super.getRootProvider();
    }

    public static boolean isArchivePath(@NonNull Path path) {
        return LocalArchiveFileSystemProvider.isArchivePath(path);
    }

    @NonNull
    public static Path getArchiveFile(@NonNull Path path) {
        return LocalArchiveFileSystemProvider.getArchiveFile(path);
    }

    public static void refresh(@NonNull Path path) {
        LocalArchiveFileSystemProvider.refresh(path);
    }

    static void doRefreshFileSystemIfNeeded(@NonNull Path path) throws RemoteFileSystemException {
        sInstance.getRootProvider().doRefreshFileSystemIfNeeded(path);
    }

    @NonNull
    public static Path getRootPathForArchiveFile(@NonNull Path archiveFile) {
        return sInstance.getLocalProvider().getRootPathForArchiveFile(archiveFile);
    }

    @NonNull
    static ArchiveFileSystem getOrNewFileSystem(@NonNull Path archiveFile) {
        return sInstance.getLocalProvider().getOrNewFileSystem(archiveFile);
    }

    void removeFileSystem(@NonNull ArchiveFileSystem fileSystem) {
        getLocalProvider().removeFileSystem(fileSystem);
    }

    static boolean supportsFileAttributeView(@NonNull Class<? extends FileAttributeView> type) {
        return LocalArchiveFileSystemProvider.supportsFileAttributeView(type);
    }
}
