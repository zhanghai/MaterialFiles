/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import androidx.annotation.NonNull;
import java8.nio.file.FileSystem;
import java8.nio.file.spi.FileSystemProvider;
import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;

public class RemoteFileServiceInterface extends IRemoteFileService.Stub {

    @NonNull
    @Override
    public IRemoteFileSystemProvider getRemoteFileSystemProviderInterface(
            @NonNull String remoteScheme) {
        FileSystemProvider provider = RemoteUtils.getLocalProvider(remoteScheme);
        return new RemoteFileSystemProviderInterface(provider);
    }

    @NonNull
    @Override
    public IRemoteFileSystem getRemoteFileSystemInterface(
            @NonNull ParcelableFileSystem remoteFileSystem) {
        FileSystem fileSystem = RemoteUtils.toLocalFileSystem(remoteFileSystem.get());
        return new RemoteFileSystemInterface(fileSystem);
    }

    @NonNull
    @Override
    public IRemotePosixFileAttributeView getRemotePosixFileAttributeViewInterface(
            @NonNull ParcelableFileAttributeView remoteAttributeView) {
        RemotePosixFileAttributeView remotePosixAttributeView = remoteAttributeView.get();
        return new RemotePosixFileAttributeViewInterface(remotePosixAttributeView.toLocal());
    }

    @Override
    public void refreshArchiveFileSystem(@NonNull ParcelableFileSystem remoteFileSystem) {
        FileSystem fileSystem = RemoteUtils.toLocalFileSystem(remoteFileSystem.get());
        ArchiveFileSystemProvider.refresh(fileSystem.getPath(""));
    }
}
