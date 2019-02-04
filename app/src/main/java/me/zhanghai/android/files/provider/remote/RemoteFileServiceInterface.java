/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.provider.remote;

import androidx.annotation.NonNull;
import java8.nio.file.FileSystem;
import java8.nio.file.spi.FileSystemProvider;
import me.zhanghai.android.files.provider.FileSystemProviders;
import me.zhanghai.android.files.provider.archive.ArchiveFileSystemProvider;
import me.zhanghai.android.files.provider.common.PosixFileAttributeView;

public class RemoteFileServiceInterface extends IRemoteFileService.Stub {

    @NonNull
    @Override
    public IRemoteFileSystemProvider getRemoteFileSystemProviderInterface(
            @NonNull String scheme) {
        FileSystemProvider provider = FileSystemProviders.get(scheme);
        return new RemoteFileSystemProviderInterface(provider);
    }

    @NonNull
    @Override
    public IRemoteFileSystem getRemoteFileSystemInterface(
            @NonNull ParcelableObject parcelableFileSystem) {
        FileSystem fileSystem = parcelableFileSystem.get();
        return new RemoteFileSystemInterface(fileSystem);
    }

    @NonNull
    @Override
    public IRemotePosixFileAttributeView getRemotePosixFileAttributeViewInterface(
            @NonNull ParcelableObject parcelableAttributeView) {
        PosixFileAttributeView attributeView = parcelableAttributeView.get();
        return new RemotePosixFileAttributeViewInterface(attributeView);
    }

    @Override
    public void refreshArchiveFileSystem(@NonNull ParcelableObject parcelableFileSystem) {
        FileSystem fileSystem = parcelableFileSystem.get();
        ArchiveFileSystemProvider.refresh(fileSystem.getPath(""));
    }
}
